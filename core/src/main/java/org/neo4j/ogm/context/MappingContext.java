/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

import java.lang.reflect.Field;
import java.util.*;

import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.utils.EntityUtils;

/**
 * The MappingContext maintains a map of all the objects created during the hydration
 * of an object map (domain hierarchy). The MappingContext lifetime is concurrent
 * with a session lifetime.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class MappingContext {

    /**
     * register of all mapped entities of a specific type (including supertypes)
     */
    private final TypeRegister typeRegister;

    private final Map<Long, Object> nodeEntityRegister;

    private final Map<LabelPrimaryId, Object> primaryIndexNodeRegister;

    private final Map<Long, Object> relationshipEntityRegister;

    private final Map<LabelPrimaryId, Object> primaryIdToRelationship;

    private final Set<MappedRelationship> relationshipRegister;

    private final Map<Long, LabelHistory> labelHistoryRegister;

    private final IdentityMap identityMap;

    private final MetaData metaData;


    public MappingContext(MetaData metaData) {
        this.metaData = metaData;
        this.identityMap = new IdentityMap(metaData);
        this.typeRegister = new TypeRegister();
        this.nodeEntityRegister = new HashMap<>();
        this.primaryIndexNodeRegister = new HashMap<>();
        this.relationshipEntityRegister = new HashMap<>();
        this.primaryIdToRelationship = new HashMap<>();
        this.relationshipRegister = new HashSet<>();
        this.labelHistoryRegister = new HashMap<>();
    }

    /**
     * Gets a node entity from the MappingContext by its graph id.
     *
     * NOTE: to get entity by field with @Id use {@link #getNodeEntityById(ClassInfo, Object)} - you also need to check if such id
     * exists and is of correct type
     *
     * @param graphId The graph id to look for.
     * @return The entity or null if not found.
     */
    public Object getNodeEntity(Long graphId) {
        return nodeEntityRegister.get(graphId);
    }

    /**
     * Get a node entity from the MappingContext by its primary id
     *
     * @param classInfo class info of the entity (this must be provided because id itself is not unique)
     * @param id primary id of the entity
     *
     * @return the entity or null if not found
     */
    public Object getNodeEntityById(ClassInfo classInfo, Object id) {
        return primaryIndexNodeRegister.get(new LabelPrimaryId(classInfo, id));
    }

    public Object addNodeEntity(Object entity, Long id) {

        if (nodeEntityRegister.putIfAbsent(id, entity) == null) {
            entity = nodeEntityRegister.get(id);
            addType(entity.getClass(), entity, id);
            remember(entity);
            collectLabelHistory(entity);
            final ClassInfo primaryIndexClassInfo = metaData.classInfo(entity);
            final FieldInfo primaryIndexField = primaryIndexClassInfo.primaryIndexField(); // also need to add the class to key to prevent collisions.
            if (primaryIndexField != null) {
                final Object primaryIndexValue = primaryIndexField.read(entity);
                if (primaryIndexValue != null) {
                    LabelPrimaryId key = new LabelPrimaryId(primaryIndexClassInfo, primaryIndexValue);
                    primaryIndexNodeRegister.putIfAbsent(key, entity);
                }
            }
        }

        return entity;
    }

    boolean removeRelationship(MappedRelationship mappedRelationship) {
        return relationshipRegister.remove(mappedRelationship);
    }

    /**
     * De-registers an object from the mapping context
     * - removes the object instance from the typeRegister(s)
     * - removes the object id from the nodeEntityRegister
     * - removes any relationship entities from relationshipEntityRegister if they have this object either as start or end node
     *
     * @param entity the object to deregister
     * @param id the id of the object in Neo4j
     */
    void removeNodeEntity(Object entity, Long id) {
        removeType(entity.getClass(), id);
        nodeEntityRegister.remove(id);
        final ClassInfo primaryIndexClassInfo = metaData.classInfo(entity);
        final FieldInfo primaryIndexField = primaryIndexClassInfo.primaryIndexField(); // also need to add the class to key to prevent collisions.
        if (primaryIndexField != null) {
            final Object primaryIndexValue = primaryIndexField.read(entity);
            if (primaryIndexValue != null) {
                primaryIndexNodeRegister.remove(new LabelPrimaryId(primaryIndexClassInfo, primaryIndexValue));
            }
        }

        deregisterDependentRelationshipEntity(entity);
    }

    public void replaceNodeEntity(Object entity, Long id) {
        nodeEntityRegister.remove(id);
        final ClassInfo primaryIndexClassInfo = metaData.classInfo(entity);
        final FieldInfo primaryIndexField = primaryIndexClassInfo.primaryIndexField(); // also need to add the class to key to prevent collisions.
        if (primaryIndexField != null) {
            final Object primaryIndexValue = primaryIndexField.read(entity);
            primaryIndexNodeRegister.remove(new LabelPrimaryId(primaryIndexClassInfo, primaryIndexValue));
        }
        addNodeEntity(entity, id);
    }

    public void replaceRelationshipEntity(Object entity, Long id) {
        relationshipEntityRegister.remove(id);
        ClassInfo classInfo = metaData.classInfo(entity);
        FieldInfo primaryIndexField = classInfo.primaryIndexField();
        if (primaryIndexField != null) {
            Object primaryId = primaryIndexField.read(entity);
            primaryIdToRelationship.remove(new LabelPrimaryId(classInfo, primaryId));
        }
        addRelationshipEntity(entity, id);
    }

    Collection<Object> getEntities(Class<?> type) {
        return typeRegister.get(type).values();
    }

    LabelHistory labelHistory(Long identity) {
        return labelHistoryRegister.computeIfAbsent(identity, k -> new LabelHistory());
    }

    public boolean isDirty(Object entity) {
        ClassInfo classInfo = metaData.classInfo(entity);
        Object id = classInfo.identityField().readProperty(entity);
        return !identityMap.remembered((Long) id, entity, classInfo);
    }

    public boolean containsRelationship(MappedRelationship relationship) {
        return relationshipRegister.contains(relationship);
    }

    public Set<MappedRelationship> getRelationships() {
        return relationshipRegister;
    }

    public void addRelationship(MappedRelationship relationship) {
        if (relationship.getRelationshipId() != null && relationshipEntityRegister.get(relationship.getRelationshipId()) == null) {
            relationship.setRelationshipId(null); //We're only interested in id's of relationship entities
        }
        relationshipRegister.add(relationship);
    }

    public void clear() {
        identityMap.clear();
        relationshipRegister.clear();
        primaryIdToRelationship.clear();
        nodeEntityRegister.clear();
        primaryIndexNodeRegister.clear();
        typeRegister.clear();
        relationshipEntityRegister.clear();
        labelHistoryRegister.clear();
    }

    public Object getRelationshipEntity(Long relationshipId) {
        return relationshipEntityRegister.get(relationshipId);
    }

    /**
     * Get a relationship entity from MappingContext by primary id
     *
     * @param classInfo classInfo of the relationship entity (it is needed to because primary id may not be unique
     * across all relationship types)
     * @param id primary id of the entity
     *
     * @return relationship entity
     */
    public Object getRelationshipEntityById(ClassInfo classInfo, Object id) {
        return primaryIdToRelationship.get(new LabelPrimaryId(classInfo, id));
    }

    public Object addRelationshipEntity(Object relationshipEntity, Long id) {

        if (relationshipEntityRegister.putIfAbsent(id, relationshipEntity) == null) {
            relationshipEntity = relationshipEntityRegister.get(id);
            addType(relationshipEntity.getClass(), relationshipEntity, id);
            remember(relationshipEntity);

            ClassInfo classInfo = metaData.classInfo(relationshipEntity);
            FieldInfo primaryIdField = classInfo.primaryIndexField();
            if (primaryIdField != null) {
                Object primaryId = primaryIdField.read(relationshipEntity);
                primaryIdToRelationship.put(new LabelPrimaryId(classInfo, primaryId), relationshipEntity);
            }
        }
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
                    removeType(classInfo.getUnderlyingClass());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            FieldInfo identityReader = classInfo.identityField();
            removeClass(type);
        }
    }


    /*
     * purges all information about a node entity with this id
     */
    public boolean detachNodeEntity(Long id) {
        Object objectToDetach = getNodeEntity(id);
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
    void removeEntity(Object entity) {
        Class<?> type = entity.getClass();
        Long id = EntityUtils.identity(entity, metaData);

        purge(entity, type);

        if (id >= 0) {
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
        FieldInfo.write(identityField, entity, null);
    }


    public Set<Object> neighbours(Object entity) {

        Set<Object> neighbours = new HashSet<>();

        Class<?> type = entity.getClass();
        ClassInfo classInfo = metaData.classInfo(type.getName());
        Long id = EntityUtils.identity(entity, metaData);

        if (id >= 0) {
            if (!metaData.isRelationshipEntity(type.getName())) {
                if (nodeEntityRegister.containsKey(id)) {
                    // todo: this will be very slow for many objects
                    // todo: refactor to create a list of mappedRelationships from a nodeEntity id.
                    for (MappedRelationship mappedRelationship : relationshipRegister) {
                        if (mappedRelationship.getStartNodeId() == id || mappedRelationship.getEndNodeId() == id) {
                            Object affectedObject = mappedRelationship.getEndNodeId() == id ? nodeEntityRegister.get(mappedRelationship.getStartNodeId()) : nodeEntityRegister.get(mappedRelationship.getEndNodeId());
                            if (affectedObject != null) {
                                neighbours.add(affectedObject);
                            }
                        }
                    }
                }
            } else if (relationshipEntityRegister.containsKey(id)) {
                FieldInfo startNodeReader = classInfo.getStartNodeReader();
                FieldInfo endNodeReader = classInfo.getEndNodeReader();
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
        Iterator<Long> relationshipEntityIdIterator = relationshipEntityRegister.keySet().iterator();
        while (relationshipEntityIdIterator.hasNext()) {
            Long relationshipEntityId = relationshipEntityIdIterator.next();
            Object relationshipEntity = relationshipEntityRegister.get(relationshipEntityId);
            final ClassInfo classInfo = metaData.classInfo(relationshipEntity);
            FieldInfo startNodeReader = classInfo.getStartNodeReader();
            FieldInfo endNodeReader = classInfo.getEndNodeReader();
            if (startOrEndEntity == startNodeReader.read(relationshipEntity) || startOrEndEntity == endNodeReader.read(relationshipEntity)) {
                relationshipEntityIdIterator.remove();
            }
        }
    }

    private void removeClass(Class<?> type) {

        for (Object entity : getEntities(type)) {
            purge(entity, type);
        }
        typeRegister.delete(type);
    }


    private void purge(Object entity, Class type) {
        Long id = EntityUtils.identity(entity, metaData);
        Set<Object> relEntitiesToPurge = new HashSet<>();
        if (id >= 0) {
            // remove a NodeEntity
            if (!metaData.isRelationshipEntity(type.getName())) {
                if (nodeEntityRegister.containsKey(id)) {
                    // remove the object from the node register
                    nodeEntityRegister.remove(id);
                    // remove all relationship mappings to/from this object
                    Iterator<MappedRelationship> mappedRelationshipIterator = relationshipRegister.iterator();
                    while (mappedRelationshipIterator.hasNext()) {
                        MappedRelationship mappedRelationship = mappedRelationshipIterator.next();
                        if (mappedRelationship.getStartNodeId() == id || mappedRelationship.getEndNodeId() == id) {

                            // first purge any RE mappings (if its a RE)
                            if (mappedRelationship.getRelationshipId() != null) {
                                Object relEntity = relationshipEntityRegister.get(mappedRelationship.getRelationshipId());
                                if (relEntity != null) {
                                    relEntitiesToPurge.add(relEntity);
                                }
                            }
                            // finally remove the mapped relationship
                            mappedRelationshipIterator.remove();
                        }
                    }
                }
            } else {
                // remove a RelationshipEntity
                if (relationshipEntityRegister.containsKey(id)) {
                    relationshipEntityRegister.remove(id);
                    final ClassInfo classInfo = metaData.classInfo(entity);
                    FieldInfo startNodeReader = classInfo.getStartNodeReader();
                    Object startNode = startNodeReader.read(entity);
                    removeEntity(startNode);
                    FieldInfo endNodeReader = classInfo.getEndNodeReader();
                    Object endNode = endNodeReader.read(entity);
                    removeEntity(endNode);
                }
            }
            for (Object relEntity : relEntitiesToPurge) {
                ClassInfo relClassInfo = metaData.classInfo(relEntity);
                FieldInfo relIdentityReader = relClassInfo.identityField();
                purge(relEntity, relClassInfo.getUnderlyingClass());
            }
        }
    }

    private void addType(Class type, Object entity, Object id) {
        typeRegister.add(metaData, type, entity, id);
    }

    private void removeType(Class type, Object id) {
        typeRegister.remove(metaData, type, id);
    }

    private void remember(Object entity) {
        ClassInfo classInfo = metaData.classInfo(entity);
        Long id = (Long) classInfo.identityField().readProperty(entity);
        identityMap.remember(id, entity, classInfo);
    }

    private void collectLabelHistory(Object entity) {
        ClassInfo classInfo = metaData.classInfo(entity);
        FieldInfo fieldInfo = classInfo.labelFieldOrNull();
        if (fieldInfo != null) {
            Collection<String> labels = (Collection<String>) fieldInfo.read(entity);
            Long id = (Long) classInfo.identityField().readProperty(entity);
            labelHistory(id).push(labels);
        }
    }
}
