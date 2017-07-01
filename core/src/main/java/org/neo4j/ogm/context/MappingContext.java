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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.classloader.MetaDataClassLoader;
import org.neo4j.ogm.context.register.EntityRegister;
import org.neo4j.ogm.context.register.LabelHistoryRegister;
import org.neo4j.ogm.context.register.TypeRegister;
import org.neo4j.ogm.entity.io.EntityAccessManager;
import org.neo4j.ogm.entity.io.FieldReader;
import org.neo4j.ogm.entity.io.FieldWriter;
import org.neo4j.ogm.entity.io.PropertyReader;
import org.neo4j.ogm.entity.io.RelationalReader;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
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

    private final EntityRegister<Long> nodeEntityRegister;

    private final EntityRegister<Object> primaryIndexNodeRegister;

    private final EntityRegister<Long> relationshipEntityRegister;

    private final Set<MappedRelationship> relationshipRegister;

    private final LabelHistoryRegister labelHistoryRegister;

    private final EntityMemo objectMemo;

    private final MetaData metaData;


    public MappingContext(MetaData metaData) {
        this.metaData = metaData;
        this.objectMemo = new EntityMemo(metaData);
        this.typeRegister = new TypeRegister();
        this.nodeEntityRegister = new EntityRegister<>();
        this.primaryIndexNodeRegister = new EntityRegister<>();
        this.relationshipEntityRegister = new EntityRegister<>();
        this.relationshipRegister = new HashSet<>();
        this.labelHistoryRegister = new LabelHistoryRegister();
    }

    public Object getNodeEntity(Object id) {

        Object result = null;

        if (id instanceof Long) {
            result =  nodeEntityRegister.get((Long)id);
        }

        if (result == null) {
            result = primaryIndexNodeRegister.get(id);
        }

        return result;
    }

    public Object addNodeEntity(Object entity, Long id) {

        if (nodeEntityRegister.add(id, entity)) {
            entity = nodeEntityRegister.get(id);
            addType(entity.getClass(), entity, id);
            remember(entity);
            collectLabelHistory(entity);
            final ClassInfo primaryIndexClassInfo = metaData.classInfo(entity);
            final FieldInfo primaryIndexField = primaryIndexClassInfo.primaryIndexField(); // also need to add the class to key to prevent collisions.
            if (primaryIndexField != null) {
                final Object primaryIndexValue = new FieldReader(primaryIndexClassInfo, primaryIndexField).read(entity);
                primaryIndexNodeRegister.add(primaryIndexValue, entity);
            }
        }


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
     * @param id the id of the object in Neo4j
     */
    public void removeNodeEntity(Object entity, Long id) {
        removeType(entity.getClass(), id);
        nodeEntityRegister.remove(id);
        final ClassInfo primaryIndexClassInfo = metaData.classInfo(entity);
        final FieldInfo primaryIndexField = primaryIndexClassInfo.primaryIndexField(); // also need to add the class to key to prevent collisions.
        if (primaryIndexField != null) {
            final Object primaryIndexValue = new FieldReader(primaryIndexClassInfo, primaryIndexField).read(entity);
            primaryIndexNodeRegister.remove(primaryIndexValue);
        }

        deregisterDependentRelationshipEntity(entity);
    }

    public void replaceNodeEntity(Object entity, Long id) {
        nodeEntityRegister.remove(id);
        final ClassInfo primaryIndexClassInfo = metaData.classInfo(entity);
        final FieldInfo primaryIndexField = primaryIndexClassInfo.primaryIndexField(); // also need to add the class to key to prevent collisions.
        if (primaryIndexField != null) {
            final Object primaryIndexValue = new FieldReader(primaryIndexClassInfo, primaryIndexField).read(entity);
            primaryIndexNodeRegister.remove(primaryIndexValue);
        }
        addNodeEntity(entity, id);
    }

    public void replaceRelationshipEntity(Object entity, Long id) {
        relationshipEntityRegister.remove(id);
        addRelationshipEntity(entity, id);
    }

    public Collection<Object> getEntities(Class<?> type) {
        return typeRegister.get(type).values();
    }

    public LabelHistory labelHistory(Long identity) {
        return labelHistoryRegister.get(identity);
    }

    public boolean isDirty(Object entity) {
        ClassInfo classInfo = metaData.classInfo(entity);
        Object id = EntityAccessManager.getIdentityPropertyReader(classInfo).readProperty(entity);
        return !objectMemo.remembered((Long) id, entity, classInfo);
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
        objectMemo.clear();
        relationshipRegister.clear();
        nodeEntityRegister.clear();
        primaryIndexNodeRegister.clear();
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
            remember(relationshipEntity);
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
                    String implementingClassName = implementingClass.name();
                    removeType(MetaDataClassLoader.loadClass(implementingClassName));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            PropertyReader identityReader = EntityAccessManager.getIdentityPropertyReader(classInfo);
            removeClass(type);
        }
    }


    /*
     * purges all information about a node entity with this id
     */
    public boolean detachNodeEntity(Object id) {
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
    public void removeEntity(Object entity) {
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
        FieldWriter.write(identityField, entity, null);
    }


    public Set<Object> neighbours(Object entity) {

        Set<Object> neighbours = new HashSet<>();

        Class<?> type = entity.getClass();
        ClassInfo classInfo = metaData.classInfo(type.getName());
        Long id = EntityUtils.identity(entity, metaData);

        if (id >= 0) {
            if (!metaData.isRelationshipEntity(type.getName())) {
                if (nodeEntityRegister.contains(id)) {
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
                if (nodeEntityRegister.contains(id)) {
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
           for (Object relEntity : relEntitiesToPurge) {
                ClassInfo relClassInfo = metaData.classInfo(relEntity);
                PropertyReader relIdentityReader = EntityAccessManager.getIdentityPropertyReader(relClassInfo);
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
        Long id = (Long) EntityAccessManager.getIdentityPropertyReader(classInfo).readProperty(entity);
        objectMemo.remember(id, entity, classInfo);
    }

    private void collectLabelHistory(Object entity) {
        ClassInfo classInfo = metaData.classInfo(entity);
        FieldInfo fieldInfo = classInfo.labelFieldOrNull();
        if (fieldInfo != null) {
            FieldReader reader = new FieldReader(classInfo, fieldInfo);
            Collection<String> labels = (Collection<String>) reader.read(entity);
            Long id = (Long) EntityAccessManager.getIdentityPropertyReader(classInfo).readProperty(entity);
            labelHistory(id).push(labels);
        }
    }
}
