/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.session.delegates;

import java.util.*;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.context.MappedRelationship;
import org.neo4j.ogm.metadata.reflect.EntityAccessManager;
import org.neo4j.ogm.metadata.reflect.RelationalReader;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.PersistenceEvent;
import org.neo4j.ogm.utils.ClassUtils;
import org.neo4j.ogm.utils.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public final class SaveEventDelegate {

    private static final Logger logger = LoggerFactory.getLogger(SaveEventDelegate.class);

    private Neo4jSession session;
    private Set<Long> visited;
    private Set<Object> preSaved;
    private Set<MappedRelationship> registeredRelationships = new HashSet<>();
    private Set<MappedRelationship> addedRelationships = new HashSet<>();
    private Set<MappedRelationship> deletedRelationships = new HashSet<>();


    public SaveEventDelegate(Neo4jSession session) {
        this.session = session;
        this.preSaved = new HashSet();
        this.visited = new HashSet();

        this.registeredRelationships.clear();
        this.registeredRelationships.addAll(session.context().getRelationships());

    }

    public void preSave(Object object) {

        if (Collection.class.isAssignableFrom(object.getClass())) {
            for (Object element : (Collection) object) {
                preSaveCheck(element);
            }
        }
        else if (object.getClass().isArray()) {
            for (Object element : (Collections.singletonList(object))) {
                preSaveCheck(element);
            }
        }
        else {
            preSaveCheck(object);
        }
    }

    public void postSave() {
        for (Object object : this.preSaved) {
            fire(Event.TYPE.POST_SAVE, object);
        }
    }

    private void preSaveCheck(Object object) {

        if (visit(object)) {
            logger.debug("visiting: {}", object);
            for (Object child : children(object)) {
                preSaveCheck(child);
            }
            if (!preSaveFired(object) && dirty(object)) {
                firePreSave(object);
            }
        } else {
            logger.debug("already visited: {}", object);
        }

        // now fire events for any objects whose relationships have been deleted from reachable ones
        // and which therefore have been possibly rendered unreachable from the object graph traversal
        for(Object other : unreachable()) {
            if (visit(other) && !preSaveFired(other)) { // only if not yet visited and not yet fired
                firePreSave(other);
            }
        }

        // fire events for existing nodes that are not dirty, but which have had an edge added:
        for (Object other: touched()) {
            if (!preSaveFired(other)) { // only if not yet already fired
                firePreSave(other);
            }
        }

    }

    private void firePreSave(Object object) {
        fire(Event.TYPE.PRE_SAVE, object);
        this.preSaved.add(object);

    }
    private void fire(Event.TYPE eventType, Object object) {
        this.session.notifyListeners(new PersistenceEvent(object, eventType));
    }

    private boolean preSaveFired(Object object) {
        return this.preSaved.contains(object);
    }

    private Set<Object> touched() {
        Set<Object> touched = new HashSet<>();

        for (MappedRelationship added : addedRelationships) {

            Object src = session.context().getNodeEntity(added.getStartNodeId());
            Object tgt = session.context().getNodeEntity(added.getEndNodeId());

            if (src != null) {
                touched.add(src);
            }

            if (tgt != null) {
                touched.add(tgt);
            }
        }

        return touched;
    }

    private Set<Object> unreachable() {

        Set<Object> unreachable = new HashSet();

        for (MappedRelationship mappedRelationship : deletedRelationships) {
            unreachable.add(session.context().getNodeEntity(mappedRelationship.getStartNodeId()));
            unreachable.add(session.context().getNodeEntity(mappedRelationship.getEndNodeId()));
        }

        return unreachable;
    }

    // registers this object as visited and returns true if it was not previously visited, false otherwise
    private boolean visit(Object object) {
        return this.visited.add(EntityUtils.identity(object, this.session.metaData()));
    }


    // returns true if the object in question is dirty (has changed)
    // an object is dirty if either or both of:
    // - its properties have changed
    // - its relationships has changed
    private boolean dirty(Object parent) {

        // have any properties changed
        if (this.session.context().isDirty(parent)) {
            logger.debug("dirty: {}", parent);
            return true;
        }

        ClassInfo parentClassInfo = this.session.metaData().classInfo(parent);

        // an RE cannot contain additional refs because hyperedges are forbidden in Neo4j
        if (!parentClassInfo.isRelationshipEntity()) {

            // build the set of mapped relationships for this object. if there any new ones, the object is dirty
            for (RelationalReader reader : relationalReaders(parent)) {

                clearPreviousRelationships(parent, reader);

                for (MappedRelationship mappable : map(parent, reader)) {
                    if (isNew(mappable)) {
                        logger.debug("added new relationship: {} to {}", mappable, parent);
                        this.addedRelationships.add(mappable);
                        return true;
                    }
                    this.registeredRelationships.add(mappable);
                    this.deletedRelationships.remove(mappable);     // no longer deleted
                }
            }

            // compare the set of current relationships with the ones in the mapping context
            // if are there any missing from the mapping context, the object is dirty because
            // a previously mapped relationship has been deleted.
            for (MappedRelationship previous : session.context().getRelationships()) {
                if (isDeleted(previous)) {
                    logger.debug("deleted: {} from {}", previous, parent);
                    return true;
                }
            }
        }

        return false;
    }

    // returns true if the specified mapped relationship is not found in the list
    // of mapped relationships that existed when the object being saved was originally loaded
    private boolean isNew(MappedRelationship mappedRelationship) {
        return !this.session.context().getRelationships().contains(mappedRelationship);
    }

    // returns true if the specified mapped relationship is not found in the list
    // of registered relationships for the current save invocation
    private boolean isDeleted(MappedRelationship mappedRelationship) {
        return !this.registeredRelationships.contains(mappedRelationship);
    }


    // remove the current relationships for this object as loaded from the mapping context
    // we expect to put them back in again later. Any differences afterwards between
    // current relationships and the main mapping context indicate that relationships
    // have been deleted since the last time the objects were loaded.
    private void clearPreviousRelationships(Object parent, RelationalReader reader) {

        Long id = EntityUtils.identity(parent, session.metaData());
        String type = reader.relationshipType();
        Class endNodeType = ClassUtils.getType(reader.typeDescriptor());

        if (reader.relationshipDirection().equals(Relationship.INCOMING)) {
            deregisterIncomingRelationship(id, type, endNodeType);
        } else if (reader.relationshipDirection().equals(Relationship.OUTGOING)) {
            deregisterOutgoingRelationship(id, type, endNodeType);
        } else {
            deregisterOutgoingRelationship(id, type, endNodeType);
            deregisterIncomingRelationship(id, type, endNodeType);
        }

    }

    private void deregisterIncomingRelationship(Long id, String relationshipType, Class endNodeType) {

        Iterator<MappedRelationship> iterator = this.registeredRelationships.iterator();

        while (iterator.hasNext()) {
            MappedRelationship mappedRelationship = iterator.next();
            if (mappedRelationship.getEndNodeId() == id && mappedRelationship.getRelationshipType().equals(relationshipType) && endNodeType.equals(mappedRelationship.getStartNodeType())) {
                deletedRelationships.add(mappedRelationship);
                iterator.remove();
            }
        }

    }

    private void deregisterOutgoingRelationship(Long id, String relationshipType, Class endNodeType) {

        Iterator<MappedRelationship> iterator = this.registeredRelationships.iterator();

        while (iterator.hasNext()) {
            MappedRelationship mappedRelationship = iterator.next();
            if (mappedRelationship.getStartNodeId() == id && mappedRelationship.getRelationshipType().equals(relationshipType) && endNodeType.equals(mappedRelationship.getEndNodeType())) {
                deletedRelationships.add(mappedRelationship);
                iterator.remove();
            }
        }
    }

    // for a given object parent, returns a list of objects referenced by this parent, i.e. its children
    // at this stage, these children may or may not represent related nodes in the graph to the parent node
    // We'll figure that out later.
    private List<Object> children(Object parent) {

        List<Object> children = new ArrayList();

        ClassInfo parentClassInfo = this.session.metaData().classInfo(parent);

        if (parentClassInfo != null) {

            for (RelationalReader reader : EntityAccessManager.getRelationalReaders(parentClassInfo)) {

                Object reference = reader.read(parent);

                if (reference != null) {
                    if (reference.getClass().isArray()) {
                        addChildren(children, Collections.singletonList(reference));
                    } else if (Collection.class.isAssignableFrom(reference.getClass())) {
                        addChildren(children, (Collection) reference);
                    } else {
                        addChild(children, reference);
                    }
                }
            }
        }

        return children;
    }

    private void addChildren(Collection children, Collection references) {
        for (Object reference : references) {
            addChild(children, reference);
        }
    }

    private void addChild(Collection children, Object reference) {
        children.add(reference);
    }


    private Collection<RelationalReader> relationalReaders(Object object) {
        return EntityAccessManager.getRelationalReaders(this.session.metaData().classInfo(object));
    }


    // given an object and a reader, returns a collection of
    // MappedRelationships from the reference or references read by the reader from
    // the parent object.
    //
    // note that even if the reference is a singleton, a collection is always
    // returned, to make it easier for the caller to handle the results.
    private Collection<MappedRelationship> map(Object parent, RelationalReader reader) {

        Set<MappedRelationship> mappedRelationships = new HashSet<>();

        Object reference = reader.read(parent);

        if (reference != null) {
            if (reference.getClass().isArray()) {
                mapCollection(mappedRelationships, parent, reader, Collections.singletonList(reference));
            } else if (Collection.class.isAssignableFrom(reference.getClass())) {
                mapCollection(mappedRelationships, parent, reader, (Collection) reference);
            } else {
                mapInstance(mappedRelationships, parent, reader, reference);
            }
        }

        return mappedRelationships;
    }


    // creates a MappedRelationship between the parent object and the reference. In the case that the reference
    // object is a RE, the relationship is created from the start node and the end node of the RE.
    // a MappedRelationship therefore represents a directed edge between two nodes in the graph.
    private void mapInstance(Set<MappedRelationship> mappedRelationships, Object parent, RelationalReader reader, Object reference) {

        String type = reader.relationshipType();
        String direction = reader.relationshipDirection();

        ClassInfo parentInfo = this.session.metaData().classInfo(parent);
        Long parentId = EntityUtils.identity(parent, session.metaData());


        ClassInfo referenceInfo = this.session.metaData().classInfo(reference);

        if (referenceInfo != null) {

            Long referenceId = EntityUtils.identity(reference, this.session.metaData());

            if (!referenceInfo.isRelationshipEntity()) {

                if (direction.equals(Relationship.OUTGOING)) {
                    MappedRelationship edge = new MappedRelationship(parentId, type, referenceId, parentInfo.getUnderlyingClass(), referenceInfo.getUnderlyingClass());
                    mappedRelationships.add (edge);
                } else {
                    MappedRelationship edge = new MappedRelationship(referenceId, type, parentId, referenceInfo.getUnderlyingClass(), parentInfo.getUnderlyingClass());
                    mappedRelationships.add (edge);
                }
            }

            else {
                // graph relationship is transitive across the RE domain object
                Object startNode =  EntityAccessManager.getStartNodeReader(referenceInfo).read(reference);
                ClassInfo startNodeInfo = this.session.metaData().classInfo(startNode);
                Long startNodeId = EntityUtils.identity(startNode, session.metaData());

                Object endNode =  EntityAccessManager.getEndNodeReader(referenceInfo).read(reference);
                ClassInfo endNodeInfo = this.session.metaData().classInfo(endNode);
                Long endNodeId = EntityUtils.identity(endNode, session.metaData());

                MappedRelationship edge = new MappedRelationship(startNodeId, type, endNodeId, referenceId, startNodeInfo.getUnderlyingClass(), endNodeInfo.getUnderlyingClass());
                mappedRelationships.add(edge);

            }
        }
    }

    private void mapCollection(Set<MappedRelationship> mappedRelationships, Object parent, RelationalReader reader, Collection references) {

        for (Object reference : references) {
            mapInstance(mappedRelationships, parent, reader, reference);
        }

    }


}
