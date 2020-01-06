/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

package org.neo4j.ogm.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.id.IdStrategy;
import org.neo4j.ogm.id.InternalIdStrategy;
import org.neo4j.ogm.id.UuidStrategy;
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

    // map Neo4j id -> entity
    private final Map<Long, Object> nodeEntityRegister;

    // map primary index value -> entity
    private final Map<LabelPrimaryId, Object> primaryIndexNodeRegister;

    // LabelPrimaryId - > native id (contains both nodes and relationship entities)
    private final Map<LabelPrimaryId, Long> primaryIdToNativeId;

    private final Map<Long, Object> relationshipEntityRegister;

    private final Map<LabelPrimaryId, Object> primaryIdToRelationship;

    private final Set<MappedRelationship> relationshipRegister;

    private final IdentityMap identityMap;

    private final MetaData metaData;

    public MappingContext(MetaData metaData) {
        this.metaData = metaData;
        this.identityMap = new IdentityMap(metaData);
        this.nodeEntityRegister = new HashMap<>();
        this.primaryIndexNodeRegister = new HashMap<>();
        this.primaryIdToNativeId = new HashMap<>();
        this.relationshipEntityRegister = new HashMap<>();
        this.primaryIdToRelationship = new HashMap<>();
        this.relationshipRegister = new HashSet<>();
    }

    /**
     * Gets a node entity from the MappingContext by its graph id.
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
     * @param id        primary id of the entity
     * @return the entity or null if not found
     */
    public Object getNodeEntityById(ClassInfo classInfo, Object id) {
        return primaryIndexNodeRegister.get(new LabelPrimaryId(classInfo, id));
    }

    /**
     * Adds an entity to the MappingContext.
     *
     * @param entity The object to add.
     * @return The object added, never null.
     */
    public Object addNodeEntity(Object entity) {
        return addNodeEntity(entity, nativeId(entity));
    }

    /**
     * Adds an entity to the MappingContext.
     *
     * @param entity The object to add.
     * @param id
     * @return The object added, never null.
     */
    public Object addNodeEntity(Object entity, Long id) {

        ClassInfo classInfo = metaData.classInfo(entity);

        if (nodeEntityRegister.putIfAbsent(id, entity) == null) {
            remember(entity);
            final FieldInfo primaryIndexField = classInfo
                .primaryIndexField(); // also need to add the class to key to prevent collisions.
            if (primaryIndexField != null) {
                final Object primaryIndexValue = primaryIndexField.read(entity);
                if (primaryIndexValue != null) {
                    LabelPrimaryId key = new LabelPrimaryId(classInfo, primaryIndexValue);
                    primaryIndexNodeRegister.putIfAbsent(key, entity);
                    primaryIdToNativeId.put(key, id);
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
     */
    void removeNodeEntity(Object entity, boolean deregisterDependentRelationshipEntity) {

        Long id = nativeId(entity);

        nodeEntityRegister.remove(id);
        final ClassInfo primaryIndexClassInfo = metaData.classInfo(entity);
        final FieldInfo primaryIndexField = primaryIndexClassInfo
            .primaryIndexField(); // also need to add the class to key to prevent collisions.
        if (primaryIndexField != null) {
            final Object primaryIndexValue = primaryIndexField.read(entity);
            if (primaryIndexValue != null) {
                primaryIndexNodeRegister.remove(new LabelPrimaryId(primaryIndexClassInfo, primaryIndexValue));
            }
        }

        if (deregisterDependentRelationshipEntity) {
            deregisterDependentRelationshipEntity(entity);
        }
    }

    public void replaceNodeEntity(Object entity, Long identity) {
        removeNodeEntity(entity, false);

        ClassInfo classInfo = metaData.classInfo(entity);
        if (classInfo.hasPrimaryIndexField()) {
            LabelPrimaryId key = new LabelPrimaryId(classInfo, classInfo.primaryIndexField().readProperty(entity));
            primaryIdToNativeId.put(key, identity);
        }

        addNodeEntity(entity, identity);
        deregisterDependentRelationshipEntity(entity);
    }

    public void replaceRelationshipEntity(Object entity, Long id) {
        relationshipEntityRegister.remove(id);
        ClassInfo classInfo = metaData.classInfo(entity);
        FieldInfo primaryIndexField = classInfo.primaryIndexField();
        if (primaryIndexField != null) {
            Object primaryId = primaryIndexField.read(entity);
            LabelPrimaryId labelPrimaryId = new LabelPrimaryId(classInfo, primaryId);
            primaryIdToRelationship.remove(labelPrimaryId);
            primaryIdToNativeId.remove(labelPrimaryId);
        }
        addRelationshipEntity(entity, id);
    }

    /**
     * Get a collection of entities or relationships registered in the current context.
     *
     * @param type The base type to search for.
     * @return The entities found. Note that the collection will contain the concrete type given as a parameter,
     * but also all entities that are assignable to it (sub types)
     */
    Collection<Object> getEntities(Class<?> type) {
        Collection<Object> result;
        if (metaData.isRelationshipEntity(type.getName())) {
            result = relationshipEntityRegister.values().stream()
                .filter((c) -> c.getClass().isAssignableFrom(type))
                .collect(Collectors.toList());
        } else {
            result = nodeEntityRegister.values().stream()
                .filter((c) -> c.getClass().isAssignableFrom(type))
                .collect(Collectors.toList());
        }
        return result;
    }

    /**
     * Return dynamic label information about the entity (@Labels). History contains a snapshot of labels the entity had
     * when registered in the context, and the current labels.
     *
     * @param entity The entity to inspect
     * @return The label information
     */
    LabelHistory labelHistory(Object entity) {
        return identityMap.labelHistory(entity, nativeId(entity));
    }

    /**
     * Check if the entity has been modified by comparing its current state to the state it had when registered.
     * TBD : describe how the hash is computed. Not sure if it is a real deep hash.
     *
     * @param entity The entity to check
     * @return true if the entity was changed, false otherwise.
     */
    public boolean isDirty(Object entity) {
        Long graphId = nativeId(entity);
        return !identityMap.remembered(entity, graphId);
    }

    public boolean containsRelationship(MappedRelationship relationship) {
        return relationshipRegister.contains(relationship);
    }

    public Set<MappedRelationship> getRelationships() {
        return relationshipRegister;
    }

    public void addRelationship(MappedRelationship relationship) {
        if (relationship.getRelationshipId() != null
            && relationshipEntityRegister.get(relationship.getRelationshipId()) == null) {
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
        relationshipEntityRegister.clear();
    }

    public Object getRelationshipEntity(Long relationshipId) {
        return relationshipEntityRegister.get(relationshipId);
    }

    /**
     * Get a relationship entity from MappingContext by primary id
     *
     * @param classInfo classInfo of the relationship entity (it is needed to because primary id may not be unique
     *                  across all relationship types)
     * @param id        primary id of the entity
     * @return relationship entity
     */
    public Object getRelationshipEntityById(ClassInfo classInfo, Object id) {
        return primaryIdToRelationship.get(new LabelPrimaryId(classInfo, id));
    }

    public Object addRelationshipEntity(Object relationshipEntity, Long id) {

        if (relationshipEntityRegister.putIfAbsent(id, relationshipEntity) == null) {
            relationshipEntity = relationshipEntityRegister.get(id);
            remember(relationshipEntity);

            ClassInfo classInfo = metaData.classInfo(relationshipEntity);
            FieldInfo primaryIdField = classInfo.primaryIndexField();
            if (primaryIdField != null) {
                Object primaryId = primaryIdField.read(relationshipEntity);
                primaryIdToRelationship.put(new LabelPrimaryId(classInfo, primaryId), relationshipEntity);
                primaryIdToNativeId.put(new LabelPrimaryId(classInfo, primaryId), id);
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
            for (Object entity : getEntities(type)) {
                purge(entity, type);
            }
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
        purge(entity, type);
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
        EntityUtils.setIdentity(entity, null, metaData);
    }

    /**
     * Get related objects of an entity / relationship. Used in deletion scenarios.
     *
     * @param entity The entity to look neighbours for.
     * @return If entity is a relationship, end and start nodes. If entity is a node, the relations pointing to it.
     */
    public Set<Object> neighbours(Object entity) {

        Set<Object> neighbours = new HashSet<>();

        Class<?> type = entity.getClass();
        Long id = nativeId(entity);

        if (id >= 0) {
            if (!metaData.isRelationshipEntity(type.getName())) {
                if (getNodeEntity(id) != null) {
                    // todo: this will be very slow for many objects
                    // todo: refactor to create a list of mappedRelationships from a nodeEntity id.
                    for (MappedRelationship mappedRelationship : relationshipRegister) {
                        if (mappedRelationship.getStartNodeId() == id || mappedRelationship.getEndNodeId() == id) {
                            Object affectedObject = mappedRelationship.getEndNodeId() == id ?
                                getNodeEntity(mappedRelationship.getStartNodeId()) :
                                getNodeEntity(mappedRelationship.getEndNodeId());
                            if (affectedObject != null) {
                                neighbours.add(affectedObject);
                            }
                        }
                    }
                }
            } else if (relationshipEntityRegister.containsKey(id)) {
                ClassInfo classInfo = metaData.classInfo(type.getName());
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
            if (startOrEndEntity == startNodeReader.read(relationshipEntity) || startOrEndEntity == endNodeReader
                .read(relationshipEntity)) {
                relationshipEntityIdIterator.remove();
            }
        }
    }

    private void purge(Object entity, Class type) {
        Long id = nativeId(entity);
        Set<Object> relEntitiesToPurge = new HashSet<>();
        if (id >= 0) {
            // remove a NodeEntity
            if (!metaData.isRelationshipEntity(type.getName())) {
                if (getNodeEntity(id) != null) {
                    // remove the object from the node register
                    removeNodeEntity(entity, false);
                    // remove all relationship mappings to/from this object
                    Iterator<MappedRelationship> mappedRelationshipIterator = relationshipRegister.iterator();
                    while (mappedRelationshipIterator.hasNext()) {
                        MappedRelationship mappedRelationship = mappedRelationshipIterator.next();
                        if (mappedRelationship.getStartNodeId() == id || mappedRelationship.getEndNodeId() == id) {

                            // first purge any RE mappings (if its a RE)
                            if (mappedRelationship.getRelationshipId() != null) {
                                Object relEntity = relationshipEntityRegister
                                    .get(mappedRelationship.getRelationshipId());
                                if (relEntity != null) {
                                    // TODO : extract the "remove a RelationshipEntity" block below in a method
                                    // and call it here instead of going recursive ?
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
                purge(relEntity, relClassInfo.getUnderlyingClass());
            }
        }
    }

    private void remember(Object entity) {
        identityMap.remember(entity, nativeId(entity));
    }

    public Long nativeId(Object entity) {
        ClassInfo classInfo = metaData.classInfo(entity);
        generateIdIfNecessary(entity, classInfo);

        if (classInfo.hasIdentityField()) {
            return EntityUtils.identity(entity, metaData);
        } else {
            FieldInfo fieldInfo = classInfo.primaryIndexField();
            Object primaryId = fieldInfo.readProperty(entity);

            if (primaryId == null) {
                throw new MappingException("Field with primary id is null for entity " + entity);
            }
            LabelPrimaryId key = new LabelPrimaryId(classInfo, primaryId);
            Long graphId = primaryIdToNativeId.get(key);
            if (graphId == null) {
                graphId = EntityUtils.nextRef();
                primaryIdToNativeId.put(key, graphId);
            }
            return graphId;
        }
    }

    private void generateIdIfNecessary(Object entity, ClassInfo classInfo) {
        if (classInfo.idStrategyClass() == null || InternalIdStrategy.class.equals(classInfo.idStrategyClass())) {
            return;
        }

        if (classInfo.idStrategy() == null) {
            throw new MappingException("Id strategy " + classInfo.idStrategyClass() + " could not be instantiated " +
                "and wasn't registered. Either provide no argument constructor or register instance " +
                "with SessionFactory");
        }
        FieldInfo primaryIndexField = classInfo.primaryIndexField();
        Object existingUuid = primaryIndexField.read(entity);
        if (existingUuid == null) {
            IdStrategy strategy = classInfo.idStrategy();
            Object id = strategy.generateId(entity);
            if (strategy instanceof UuidStrategy && primaryIndexField.isTypeOf(String.class)) {
                id = id.toString();
            }
            primaryIndexField.writeDirect(entity, id);
        }
    }
}
