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

package org.neo4j.ogm.context;

import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.classloader.MetaDataClassLoader;
import org.neo4j.ogm.context.register.EntityRegister;
import org.neo4j.ogm.context.register.LabelHistoryRegister;
import org.neo4j.ogm.context.register.RelationshipRegister;
import org.neo4j.ogm.context.register.TypeRegister;
import org.neo4j.ogm.entity.io.*;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;

import java.lang.reflect.Field;
import java.util.*;

/**
 * The MappingContext maintains a map of all the objects created during the hydration
 * of an object map (domain hierarchy). The MappingContext lifetime is concurrent
 * with a session lifetime.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class MappingContext {

    /**
     * register of all mapped entities of a specific type (including supertypes)
     */
    private final TypeRegister typeRegister = new TypeRegister();
    private final EntityRegister nodeEntityRegister = new EntityRegister();
    private final EntityRegister relationshipEntityRegister = new EntityRegister();
    private final RelationshipRegister relationshipRegister = new RelationshipRegister();
    private final LabelHistoryRegister labelHistoryRegister = new LabelHistoryRegister();

    private final EntityMemo objectMemo;

    private final MetaData metaData;


    public MappingContext(MetaData metaData) {
        this.metaData = metaData;
        this.objectMemo = new EntityMemo(metaData);
    }

    public Object getNodeEntity(Long id) {
        return nodeEntityRegister.get(id);
    }

    public Object addNodeEntity(Object entity, Long id) {
        if (nodeEntityRegister.add(id, entity)) {
            entity = nodeEntityRegister.get(id);
            addType(entity.getClass(), entity, id);
        }
        remember(entity);
        return entity;
    }

    public boolean removeRelationship(MappedRelationship mappedRelationship) {
        return relationshipRegister.remove(mappedRelationship);
    }

    /**
     * De-registers an object from the mapping context
     * - removes the object instance from the typeRegister(s)
     * - removes the object id from the nodeEntityRegister
     * - removes any relationship entities from relationshipEntityRegister if they have this object either as start or end node
     *
     * @param entity the object to deregister
     * @param id     the id of the object in Neo4j
     */
    public void removeNodeEntity(Object entity, Long id) {
        removeType(entity.getClass(), id);
        nodeEntityRegister.remove(id);
        deregisterDependentRelationshipEntity(entity);
    }

    public void replaceNodeEntity(Object entity, Long id) {
        nodeEntityRegister.remove(id);
        addNodeEntity(entity, id);
        remember(entity);
    }

    public Collection<Object> getEntities(Class<?> type) {
        return typeRegister.get(type).values();
    }

    public LabelHistory labelHistory(Long identity) {
        return labelHistoryRegister.get(identity);
    }

    public boolean isDirty(Object entity) {
        ClassInfo classInfo = metaData.classInfo(entity);
        Object id = EntityAccessManager.getIdentityPropertyReader(classInfo).read(entity);
        return !objectMemo.remembered((Long) id, entity, classInfo);
    }

    public boolean containsRelationship(MappedRelationship relationship) {
        return relationshipRegister.contains(relationship);
    }

    public Set<MappedRelationship> getRelationships() {
        return relationshipRegister.values();
    }

    public void addRelationship(MappedRelationship relationship) {
        if (relationship.getRelationshipId() != null && relationshipEntityRegister.get(relationship.getRelationshipId()) == null) {
            relationship.setRelationshipId(null); //We're only interested in id's of relationship entities
        }
        relationshipRegister.add(relationship);
    }

    public void clear() {
        objectMemo.clear();
        relationshipRegister.clear();
        nodeEntityRegister.clear();
        typeRegister.clear();
        relationshipEntityRegister.clear();
        labelHistoryRegister.clear();
    }

    public Object getRelationshipEntity(Long relationshipId) {
        return relationshipEntityRegister.get(relationshipId);
    }

    public Object addRelationshipEntity(Object relationshipEntity, Long id) {
        if (relationshipEntityRegister.add(id, relationshipEntity)) {
            relationshipEntity = relationshipEntityRegister.get(id);
            addType(relationshipEntity.getClass(), relationshipEntity, id);
        }
        remember(relationshipEntity);
        return relationshipEntity;
    }

    /**
     * purges all information about objects of the supplied type
     * from the mapping context. If the type is an interface, purges all implementing classes
     * in the interface hierarchy
     *
     * @param type the type whose object references and relationship mappings we want to purge
     */
    public void removeType(Class<?> type) {

        ClassInfo classInfo = metaData.classInfo(type.getName());

        if (classInfo.isInterface()) {
            List<ClassInfo> implementingClasses = metaData.getImplementingClassInfos(classInfo.name());
            for (ClassInfo implementingClass : implementingClasses) {
                try {
                    String implementingClassName = implementingClass.name();
                    removeType(MetaDataClassLoader.loadClass(implementingClassName));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            PropertyReader identityReader = EntityAccessManager.getIdentityPropertyReader(classInfo);
            removeType(type, identityReader);
        }
    }


    /*
     * purges all information about a node entity with this id
     */
    public boolean detachNodeEntity(Long id) {
        Object objectToDetach = nodeEntityRegister.get(id);
        if (objectToDetach != null) {
            removeEntity(objectToDetach);
            return true;
        }
        return false;
    }

    /*
     * purges all information about a relationship entity with this id
     */
    public boolean detachRelationshipEntity(Long id) {
        Object objectToDetach = relationshipEntityRegister.get(id);
        if (objectToDetach != null) {
            removeEntity(objectToDetach);
            return true;
        }
        return false;
    }

    /**
     * removes all information about this object from the mapping context
     *
     * @param entity the instance whose references and relationship mappings we want to purge
     */
    public void removeEntity(Object entity) {
        Class<?> type = entity.getClass();
        ClassInfo classInfo = metaData.classInfo(type.getName());
        PropertyReader identityReader = EntityAccessManager.getIdentityPropertyReader(classInfo);
        Long id = (Long) identityReader.read(entity);

        purge(entity, identityReader, type);

        if (id != null) {
            typeRegister.remove(metaData, type, id);
        }
    }

    /**
     * purges all information about this object from the mapping context
     * and also sets its id to null. Should be called for new objects that have had their
     * id assigned as part of a long-running transaction, if that transaction is subsequently
     * rolled back.
     *
     * @param entity the instance whose references and relationship mappings we want to reset
     */
    public void reset(Object entity) {
        removeEntity(entity);
        Class<?> type = entity.getClass();
        ClassInfo classInfo = metaData.classInfo(type.getName());
        Field identityField = classInfo.getField(classInfo.identityField());
        FieldWriter.write(identityField, entity, null);
    }


    public Set<Object> neighbours(Object entity) {

        Set<Object> neighbours = new HashSet<>();

        Class<?> type = entity.getClass();
        ClassInfo classInfo = metaData.classInfo(type.getName());
        PropertyReader identityReader = EntityAccessManager.getIdentityPropertyReader(classInfo);

        Long id = (Long) identityReader.read(entity);

        if (id != null) {
            if (!metaData.isRelationshipEntity(type.getName())) {
                if (nodeEntityRegister.contains(id)) {
                    // todo: this will be very slow for many objects
                    // todo: refactor to create a list of mappedRelationships from a nodeEntity id.
                    for (MappedRelationship mappedRelationship : getRelationships()) {
                        if (mappedRelationship.getStartNodeId() == id || mappedRelationship.getEndNodeId() == id) {
                            Object affectedObject = mappedRelationship.getEndNodeId() == id ? nodeEntityRegister.get(mappedRelationship.getStartNodeId()) : nodeEntityRegister.get(mappedRelationship.getEndNodeId());
                            if (affectedObject != null) {
                                neighbours.add(affectedObject);
                            }
                        }
                    }
                }
            } else if (relationshipEntityRegister.contains(id)) {
                RelationalReader startNodeReader = EntityAccessManager.getStartNodeReader(classInfo);
                RelationalReader endNodeReader = EntityAccessManager.getEndNodeReader(classInfo);
                neighbours.add(startNodeReader.read(entity));
                neighbours.add(endNodeReader.read(entity));
            }
        }

        return neighbours;
    }

    /**
     * Deregister a relationship entity if it has either start or end node equal to the supplied startOrEndEntity
     *
     * @param startOrEndEntity the entity that might be the start or end node of a relationship entity
     */
    private void deregisterDependentRelationshipEntity(Object startOrEndEntity) {
        Iterator<Long> relationshipEntityIdIterator = relationshipEntityRegister.iterator();
        while (relationshipEntityIdIterator.hasNext()) {
            Long relationshipEntityId = relationshipEntityIdIterator.next();
            Object relationshipEntity = relationshipEntityRegister.get(relationshipEntityId);
            RelationalReader startNodeReader = EntityAccessManager.getStartNodeReader(metaData.classInfo(relationshipEntity));
            RelationalReader endNodeReader = EntityAccessManager.getEndNodeReader(metaData.classInfo(relationshipEntity));
            if (startOrEndEntity == startNodeReader.read(relationshipEntity) || startOrEndEntity == endNodeReader.read(relationshipEntity)) {
                relationshipEntityIdIterator.remove();
            }
        }
    }

    private void removeType(Class<?> type, PropertyReader identityReader) {

        for (Object entity : getEntities(type)) {
            purge(entity, identityReader, type);
        }
        typeRegister.delete(type);

    }

    private void purge(Object entity, PropertyReader identityReader, Class type) {
        Long id = (Long) identityReader.read(entity);
        if (id != null) {
            // remove a NodeEntity
            if (!metaData.isRelationshipEntity(type.getName())) {
                if (nodeEntityRegister.contains(id)) {
                    // remove the object from the node register
                    nodeEntityRegister.remove(id);
                    // remove all relationship mappings to/from this object
                    Iterator<MappedRelationship> mappedRelationshipIterator = getRelationships().iterator();
                    while (mappedRelationshipIterator.hasNext()) {
                        MappedRelationship mappedRelationship = mappedRelationshipIterator.next();
                        if (mappedRelationship.getStartNodeId() == id || mappedRelationship.getEndNodeId() == id) {

                            // first purge any RE mappings (if its a RE)
                            if (mappedRelationship.getRelationshipId() != null) {
                                Object relEntity = relationshipEntityRegister.get(mappedRelationship.getRelationshipId());
                                if (relEntity != null) {
                                    String relType = mappedRelationship.getRelationshipType();
                                    ClassInfo relClassInfo = metaData.classInfo(relType);
                                    PropertyReader relIdentityReader = EntityAccessManager.getIdentityPropertyReader(relClassInfo);
                                    purge(relEntity, relIdentityReader, relClassInfo.getUnderlyingClass());
                                }
                            }

                            // finally remove the mapped relationship
                            relationshipRegister.remove(mappedRelationship);
                        }
                    }
                }
            } else {
                // remove a RelationshipEntity
                if (relationshipEntityRegister.contains(id)) {
                    relationshipEntityRegister.remove(id);
                    RelationalReader startNodeReader = EntityAccessManager.getStartNodeReader(metaData.classInfo(entity));
                    Object startNode = startNodeReader.read(entity);
                    removeEntity(startNode);
                    RelationalReader endNodeReader = EntityAccessManager.getEndNodeReader(metaData.classInfo(entity));
                    Object endNode = endNodeReader.read(entity);
                    removeEntity(endNode);
                }
            }
        }

    }

    private void addType(Class type, Object entity, Long id) {
        typeRegister.add(metaData, type, entity, id);
    }

    private void removeType(Class type, Long id) {
        typeRegister.remove(metaData, type, id);
    }

    private void remember(Object entity) {
        ClassInfo classInfo = metaData.classInfo(entity);
        Long id = (Long) EntityAccessManager.getIdentityPropertyReader(classInfo).read(entity);
        objectMemo.remember(id, entity, classInfo);
        FieldInfo fieldInfo = classInfo.labelFieldOrNull();
        if (fieldInfo != null) {
            FieldReader reader = new FieldReader(classInfo, fieldInfo);
            Collection<String> labels = (Collection<String>) reader.read(entity);
            labelHistory(id).push(labels);
        }
    }



}
