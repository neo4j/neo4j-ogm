/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.session.delegates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.context.MappedRelationship;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.DescriptorMappings;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.PersistenceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
final class SaveEventDelegate extends SessionDelegate {

    private static final Logger logger = LoggerFactory.getLogger(SaveEventDelegate.class);

    private final Set<Object> visited;
    private final Set<Object> preSaved;
    private final Set<MappedRelationship> registeredRelationships;
    private final Set<MappedRelationship> addedRelationships;
    private final Set<MappedRelationship> deletedRelationships;

    SaveEventDelegate(Neo4jSession session) {
        super(session);

        this.visited = new HashSet<>();
        this.preSaved = new HashSet<>();

        this.registeredRelationships = new HashSet<>(session.context().getRelationships());
        this.addedRelationships = new HashSet<>();
        this.deletedRelationships = new HashSet<>();
    }

    void preSave(Object object) {

        this.preSaveCheck(object);

        // now fire events for any objects whose relationships have been deleted from reachable ones
        // and which therefore have been possibly rendered unreachable from the object graph traversal
        for (Object other : unreachable()) {
            if (visit(other) && !preSaveFired(other)) { // only if not yet visited and not yet fired
                firePreSave(other);
            }
        }

        // fire events for existing nodes that are not dirty, but which have had an edge added:
        for (Object other : touched()) {
            if (!preSaveFired(other)) { // only if not yet already fired
                firePreSave(other);
            }
        }
    }

    void postSave() {
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
        } else if (logger.isDebugEnabled()) {
            logger.debug("already visited: {}", object);
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

        Set<Object> unreachable = new HashSet<>();

        for (MappedRelationship mappedRelationship : deletedRelationships) {

            logger.debug("unreachable start {} end {}", mappedRelationship.getStartNodeId(),
                mappedRelationship.getEndNodeId());

            addUnreachable(unreachable, mappedRelationship.getStartNodeId());
            addUnreachable(unreachable, mappedRelationship.getEndNodeId());
        }

        return unreachable;
    }

    private void addUnreachable(Set<Object> unreachable, long nodeId) {
        Object entity = session.context().getNodeEntity(nodeId);
        if (entity != null) {
            unreachable.add(entity);
        } else if (logger.isWarnEnabled()) {
            logger.warn("Relationship to/from entity id={} deleted, but entity is not " +
                "in context - no events will be fired.", nodeId);
        }
    }

    // registers this object as visited and returns true if it was not previously visited, false otherwise
    private boolean visit(Object object) {
        return this.visited.add(session.context().nativeId(object));
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
            for (FieldInfo reader : relationalReaders(parent)) {

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
    private void clearPreviousRelationships(Object parent, FieldInfo reader) {

        Long id = session.context().nativeId(parent);
        String type = reader.relationshipType();
        Class endNodeType = DescriptorMappings.getType(reader.typeDescriptor());

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
            if (mappedRelationship.getEndNodeId() == id && mappedRelationship.getRelationshipType()
                .equals(relationshipType) && endNodeType.equals(mappedRelationship.getStartNodeType())) {
                deletedRelationships.add(mappedRelationship);
                iterator.remove();
            }
        }
    }

    private void deregisterOutgoingRelationship(Long id, String relationshipType, Class endNodeType) {

        Iterator<MappedRelationship> iterator = this.registeredRelationships.iterator();

        while (iterator.hasNext()) {
            MappedRelationship mappedRelationship = iterator.next();
            if (mappedRelationship.getStartNodeId() == id && mappedRelationship.getRelationshipType()
                .equals(relationshipType) && endNodeType.equals(mappedRelationship.getEndNodeType())) {
                deletedRelationships.add(mappedRelationship);
                iterator.remove();
            }
        }
    }

    // for a given object parent, returns a list of objects referenced by this parent, i.e. its children
    // at this stage, these children may or may not represent related nodes in the graph to the parent node
    // We'll figure that out later.
    private List<Object> children(Object parent) {

        List<Object> children = new ArrayList<>();

        ClassInfo parentClassInfo = this.session.metaData().classInfo(parent);

        if (parentClassInfo != null) {

            for (FieldInfo reader : parentClassInfo.relationshipFields()) {

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

    private void addChildren(Collection<Object> children, Collection references) {
        for (Object reference : references) {
            addChild(children, reference);
        }
    }

    private void addChild(Collection<Object> children, Object reference) {
        children.add(reference);
    }

    private Collection<FieldInfo> relationalReaders(Object object) {
        return this.session.metaData().classInfo(object).relationshipFields();
    }

    // given an object and a reader, returns a collection of
    // MappedRelationships from the reference or references read by the reader from
    // the parent object.
    //
    // note that even if the reference is a singleton, a collection is always
    // returned, to make it easier for the caller to handle the results.
    private Collection<MappedRelationship> map(Object parent, FieldInfo reader) {

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
    private void mapInstance(Set<MappedRelationship> mappedRelationships, Object parent, FieldInfo reader,
        Object reference) {

        String type = reader.relationshipType();
        String direction = reader.relationshipDirection();

        ClassInfo parentInfo = this.session.metaData().classInfo(parent);
        Long parentId = session.context().nativeId(parent);

        ClassInfo referenceInfo = this.session.metaData().classInfo(reference);

        if (referenceInfo != null) {

            Long referenceId = session.context().nativeId(reference);

            if (!referenceInfo.isRelationshipEntity()) {

                if (direction.equals(Relationship.OUTGOING)) {
                    MappedRelationship edge = new MappedRelationship(parentId, type, referenceId,
                        null, parentInfo.getUnderlyingClass(), referenceInfo.getUnderlyingClass());
                    mappedRelationships.add(edge);
                } else {
                    MappedRelationship edge = new MappedRelationship(referenceId, type, parentId,
                        null, referenceInfo.getUnderlyingClass(), parentInfo.getUnderlyingClass());
                    mappedRelationships.add(edge);
                }
            } else {
                // graph relationship is transitive across the RE domain object
                Object startNode = referenceInfo.getStartNodeReader().read(reference);
                ClassInfo startNodeInfo = this.session.metaData().classInfo(startNode);
                Long startNodeId = session.context().nativeId(startNode);

                Object endNode = referenceInfo.getEndNodeReader().read(reference);
                ClassInfo endNodeInfo = this.session.metaData().classInfo(endNode);
                Long endNodeId = session.context().nativeId(endNode);

                MappedRelationship edge = new MappedRelationship(startNodeId, type, endNodeId, referenceId,
                    startNodeInfo.getUnderlyingClass(), endNodeInfo.getUnderlyingClass());
                mappedRelationships.add(edge);
            }
        }
    }

    private void mapCollection(Set<MappedRelationship> mappedRelationships, Object parent, FieldInfo reader,
        Collection references) {

        for (Object reference : references) {
            mapInstance(mappedRelationships, parent, reader, reference);
        }
    }
}
