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

import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.annotations.*;
import org.neo4j.ogm.classloader.MetaDataClassLoader;
import org.neo4j.ogm.metadata.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The MappingContext maintains a map of all the objects created during the hydration
 * of an object map (domain hierarchy). The MappingContext lifetime is concurrent
 * with a session lifetime.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class MappingContext {

    private final Logger logger = LoggerFactory.getLogger(MappingContext.class);

    private final ConcurrentMap<Long, Object> relationshipEntityRegister = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Object> nodeEntityRegister = new ConcurrentHashMap<>();
    private final Set<MappedRelationship> relationshipRegister = Collections.newSetFromMap(
            new ConcurrentHashMap<MappedRelationship, Boolean>());

    /**
     * register of all mapped entities of a specific type (including supertypes)
     */
    private final ConcurrentMap<Class<?>, Map<Long, Object>> typeRegister = new ConcurrentHashMap<>();
    private final EntityMemo objectMemo;

    private final MetaData metaData;
    private final EntityAccessStrategy entityAccessStrategy = new DefaultEntityAccessStrategy();

    //TODO: When CYPHER supports REMOVE ALL labels, we can stop tracking label changes
    private final ConcurrentHashMap<Long, Collection<String>> labelRegister = new ConcurrentHashMap<>();

    public MappingContext(MetaData metaData) {
        this.metaData = metaData;
        this.objectMemo = new EntityMemo(metaData);
    }

    public Object getNodeEntity(Long id) {
        return nodeEntityRegister.get(id);
    }

    // why don't we put anything in the object memo? it clearly belongs here
    public Object registerNodeEntity(Object entity, Long id) {
        if (nodeEntityRegister.putIfAbsent(id, entity) == null) {
            entity = nodeEntityRegister.get(id);
            registerTypes(entity.getClass(), entity, id);
        }
        remember(entity);
        return entity;
    }

    private void registerTypes(Class type, Object entity, Long id) {
        getAll(type).put(id, entity);
        if (type.getSuperclass() != null
                && metaData != null
                && metaData.classInfo(type.getSuperclass().getName()) != null
                && !type.getSuperclass().getName().equals("java.lang.Object")) {
            registerTypes(type.getSuperclass(), entity, id);
        }
        if (type.getInterfaces() != null
                && metaData != null) {
            for (Class interfaceClass : type.getInterfaces()) {
                if (metaData.classInfo(interfaceClass.getName()) != null) {
                    registerTypes(interfaceClass, entity, id);
                }
            }
        }
    }

    private void deregisterTypes(Class type, Long id) {
        Map<Long, Object> entities = typeRegister.get(type);
        if (entities != null) {
            if (type.getSuperclass() != null && metaData != null && metaData.classInfo(type.getSuperclass().getName()) != null && !type.getSuperclass().getName().equals("java.lang.Object")) {
                entities.remove(id);
                deregisterTypes(type.getSuperclass(), id);
            }
        }
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
    public void deregister(Object entity, Long id) {
        deregisterTypes(entity.getClass(), id);
        nodeEntityRegister.remove(id);
        deregisterDependentRelationshipEntity(entity);
    }

    public void replace(Object entity, Long id) {
        nodeEntityRegister.remove(id);
        registerNodeEntity(entity, id);
        remember(entity);
    }

    public Map<Long, Object> getAll(Class<?> type) {
        Map<Long, Object> objectList = typeRegister.get(type);
        if (objectList == null) {
            typeRegister.putIfAbsent(type, Collections.synchronizedMap(new HashMap<Long, Object>()));
            objectList = typeRegister.get(type);
        }
        return objectList;
    }

    private void remember(Object entity) {
        ClassInfo classInfo = metaData.classInfo(entity);
        Object id = entityAccessStrategy.getIdentityPropertyReader(classInfo).read(entity);
        objectMemo.remember((Long) id, entity, classInfo);
        FieldInfo fieldInfo = classInfo.labelFieldOrNull();
        if (fieldInfo != null) {
            FieldReader reader = new FieldReader(classInfo, fieldInfo);
            labelRegister.put((Long) id, (Collection<String>) reader.read(entity));
        }
    }

    public Collection<String> labels(Long identity) {
        return labelRegister.get(identity);
    }

    public boolean isDirty(Object entity) {
        ClassInfo classInfo = metaData.classInfo(entity);
        Object id = entityAccessStrategy.getIdentityPropertyReader(classInfo).read(entity);
        return !objectMemo.remembered((Long) id, entity, classInfo);
    }

    // these methods belong on the relationship registry
    public boolean isRegisteredRelationship(MappedRelationship relationship) {
        return relationshipRegister.contains(relationship);
    }

    public Set<MappedRelationship> mappedRelationships() {
        return relationshipRegister;
    }

    public void registerRelationship(MappedRelationship relationship) {
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
        labelRegister.clear();
    }

    public Object getRelationshipEntity(Long relationshipId) {
        return relationshipEntityRegister.get(relationshipId);
    }

    public Object registerRelationshipEntity(Object relationshipEntity, Long id) {
        relationshipEntityRegister.putIfAbsent(id, relationshipEntity);
        registerTypes(relationshipEntity.getClass(), relationshipEntity, id);
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
    public void clear(Class<?> type) {

        ClassInfo classInfo = metaData.classInfo(type.getName());

        if (classInfo.isInterface()) {
            List<ClassInfo> implementingClasses = metaData.getImplementingClassInfos(classInfo.name());
            for (ClassInfo implementingClass : implementingClasses) {
                try {
                    String implementingClassName = implementingClass.name();
                    clear(MetaDataClassLoader.loadClass(implementingClassName));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            PropertyReader identityReader = entityAccessStrategy.getIdentityPropertyReader(classInfo);
            clear(type, identityReader);
        }
    }


    /*
     * purges all information about a node entity with this id
     */
    public boolean detachNodeEntity(Long id) {
        Object objectToDetach = nodeEntityRegister.get(id);
        if (objectToDetach != null) {
            clear(objectToDetach);
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
            clear(objectToDetach);
            return true;
        }
        return false;
    }

    /**
     * purges all information about this object from the mapping context
     *
     * @param entity the instance whose references and relationship mappings we want to purge
     */
    public void clear(Object entity) {
        Class<?> type = entity.getClass();
        ClassInfo classInfo = metaData.classInfo(type.getName());
        PropertyReader identityReader = entityAccessStrategy.getIdentityPropertyReader(classInfo);
        Long id = (Long) identityReader.read(entity);

        purge(entity, identityReader, type);

        if (id != null) {
            getAll(type).remove(id);
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
        clear(entity);
        Class<?> type = entity.getClass();
        ClassInfo classInfo = metaData.classInfo(type.getName());
        Field identityField = classInfo.getField(classInfo.identityField());
        FieldWriter.write(identityField, entity, null);
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
            RelationalReader startNodeReader = entityAccessStrategy.getStartNodeReader(metaData.classInfo(relationshipEntity));
            RelationalReader endNodeReader = entityAccessStrategy.getEndNodeReader(metaData.classInfo(relationshipEntity));
            if (startOrEndEntity == startNodeReader.read(relationshipEntity) || startOrEndEntity == endNodeReader.read(relationshipEntity)) {
                relationshipEntityIdIterator.remove();
            }
        }
    }

    private void clear(Class<?> type, PropertyReader identityReader) {

        for (Object entity : getAll(type).values()) {
            purge(entity, identityReader, type);
        }
        getAll(type).clear();

    }

    private void purge(Object entity, PropertyReader identityReader, Class type) {
        Long id = (Long) identityReader.read(entity);
        if (id != null) {
            if (!metaData.isRelationshipEntity(type.getName())) {
                if (nodeEntityRegister.containsKey(id)) {
                    nodeEntityRegister.remove(id);
                    // remove all relationship mappings to/from this object
                    Iterator<MappedRelationship> mappedRelationshipIterator = mappedRelationships().iterator();
                    while (mappedRelationshipIterator.hasNext()) {
                        MappedRelationship mappedRelationship = mappedRelationshipIterator.next();
                        if (mappedRelationship.getStartNodeId() == id || mappedRelationship.getEndNodeId() == id) {

                            // first purge any RE mappings (if its a RE)
                            if (mappedRelationship.getRelationshipId() != null) {
                                Object relEntity = relationshipEntityRegister.get(mappedRelationship.getRelationshipId());
                                if (relEntity != null) {
                                    String relType = mappedRelationship.getRelationshipType();
                                    ClassInfo relClassInfo = metaData.classInfo(relType);
                                    PropertyReader relIdentityReader = entityAccessStrategy.getIdentityPropertyReader(relClassInfo);
                                    purge(relEntity, relIdentityReader, relClassInfo.getUnderlyingClass());
                                }
                            }

                            // finally remove the mapped relationship
                            mappedRelationshipIterator.remove();
                        }
                    }
                }
            } else {
                if (relationshipEntityRegister.containsKey(id)) {
                    relationshipEntityRegister.remove(id);
                    RelationalReader startNodeReader = entityAccessStrategy.getStartNodeReader(metaData.classInfo(entity));
                    Object startNode = startNodeReader.read(entity);
                    clear(startNode);
                    RelationalReader endNodeReader = entityAccessStrategy.getEndNodeReader(metaData.classInfo(entity));
                    Object endNode = endNodeReader.read(entity);
                    clear(endNode);
                }
            }
        }

    }

    public Set<Object> neighbours(Object entity) {

        Set<Object> neighbours = new HashSet<>();

        Class<?> type = entity.getClass();
        ClassInfo classInfo = metaData.classInfo(type.getName());
        PropertyReader identityReader = entityAccessStrategy.getIdentityPropertyReader(classInfo);

        Long id = (Long) identityReader.read(entity);

        if (id != null) {
            if (!metaData.isRelationshipEntity(type.getName())) {
                if (nodeEntityRegister.containsKey(id)) {
                    // todo: this will be very slow for many objects
                    // todo: refactor to create a list of mappedRelationships from a nodeEntity id.
                    Iterator<MappedRelationship> mappedRelationshipIterator = mappedRelationships().iterator();
                    while (mappedRelationshipIterator.hasNext()) {
                        MappedRelationship mappedRelationship = mappedRelationshipIterator.next();
                        if (mappedRelationship.getStartNodeId() == id || mappedRelationship.getEndNodeId() == id) {
                            Object affectedObject = mappedRelationship.getEndNodeId() == id ? nodeEntityRegister.get(mappedRelationship.getStartNodeId()) : nodeEntityRegister.get(mappedRelationship.getEndNodeId());
                            if (affectedObject != null) {
                                neighbours.add(affectedObject);
                            }
                        }
                    }
                }
            } else if (relationshipEntityRegister.containsKey(id)) {
                RelationalReader startNodeReader = entityAccessStrategy.getStartNodeReader(classInfo);
                RelationalReader endNodeReader = entityAccessStrategy.getEndNodeReader(classInfo);
                neighbours.add(startNodeReader.read(entity));
                neighbours.add(endNodeReader.read(entity));
            }
        }

        return neighbours;
    }
}
