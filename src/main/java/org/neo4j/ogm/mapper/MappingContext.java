/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.neo4j.ogm.entityaccess.DefaultEntityAccessStrategy;
import org.neo4j.ogm.entityaccess.EntityAccessStrategy;
import org.neo4j.ogm.entityaccess.PropertyReader;
import org.neo4j.ogm.entityaccess.RelationalReader;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.classloader.MetaDataClassLoader;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MappingContext maintains a map of all the objects created during the hydration
 * of an object map (domain hierarchy). The MappingContext lifetime is concurrent
 * with a session lifetime.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class MappingContext {

    // we need multiple registers whose purpose is obvious from their names:

    // NodeEntityRegister               register of domain entities whose properties are to be stored on Nodes
    // RelationshipEntityRegister       register of domain entities whose properties are to be stored on Relationships
    // RelationshipRegister             register of relationships between NodeEntities (i.e. as they are in the graph)

    private final Logger logger = LoggerFactory.getLogger(MappingContext.class);

    private final ConcurrentMap<Long, Object> relationshipEntityRegister = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Object> nodeEntityRegister = new ConcurrentHashMap<>();
    private final Set<MappedRelationship> relationshipRegister =  Collections.newSetFromMap(new ConcurrentHashMap<MappedRelationship, Boolean>());

    /** register of all mapped entities of a specific type (including supertypes) */
    private final ConcurrentMap<Class<?>, Map<Long,Object>> typeRegister = new ConcurrentHashMap<>();
    private final EntityMemo objectMemo;

    private final MetaData metaData;
    private final EntityAccessStrategy entityAccessStrategy = new DefaultEntityAccessStrategy();

    public MappingContext(MetaData metaData) {
        this.metaData = metaData;
        objectMemo = new EntityMemo(metaData);
    }

    public Object getNodeEntity(Long id) {
        return nodeEntityRegister.get(id);
    }

    public Object registerNodeEntity(Object entity, Long id) {
        nodeEntityRegister.putIfAbsent(id, entity);
        entity = nodeEntityRegister.get(id);
        registerTypes(entity.getClass(), entity, id);
        return entity;
    }

    private void registerTypes(Class type, Object entity, Long id) {
        getAll(type).put(id,entity);
        if (type.getSuperclass() != null
                && metaData != null
                && metaData.classInfo(type.getSuperclass().getName()) != null
                && !type.getSuperclass().getName().equals("java.lang.Object")) {
            registerTypes(type.getSuperclass(), entity, id);
        }
        if(type.getInterfaces() != null
                && metaData!=null) {
            for(Class interfaceClass : type.getInterfaces()) {
                if(metaData.classInfo(interfaceClass.getName())!=null) {
                    registerTypes(interfaceClass,entity, id);
                }
            }
        }
    }


    private void deregisterTypes(Class type, Long id) {
        Map<Long,Object> entities = typeRegister.get(type);
        if (entities != null) {
            if (type.getSuperclass() != null && metaData != null && metaData.classInfo(type.getSuperclass().getName()) != null && !type.getSuperclass().getName().equals("java.lang.Object")) {
                entities.remove(id);
                deregisterTypes(type.getSuperclass(), id);
            }
        }
    }

    /**
     * Deregisters an object from the mapping context
     * - removes the object instance from the typeRegister(s)
     * - removes the object id from the nodeEntityRegister
     * - removes any relationship entities from relationshipEntityRegister if they have this object either as start or end node
     *
     * @param entity the object to deregister
     * @param id the id of the object in Neo4j
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

    public Map<Long,Object> getAll(Class<?> type) {
        Map<Long,Object> objectList = typeRegister.get(type);
        if (objectList == null) {
            typeRegister.putIfAbsent(type, Collections.synchronizedMap(new HashMap<Long, Object>()));
            objectList = typeRegister.get(type);
        }
        return objectList;
    }

    // object memorisations
    public void remember(Object entity) {
        Object id = entityAccessStrategy.getIdentityPropertyReader(metaData.classInfo(entity)).read(entity);
        assert id != null;
        objectMemo.remember((Long)id, entity, metaData.classInfo(entity));
    }

    public boolean isDirty(Object entity) {
        ClassInfo classInfo = metaData.classInfo(entity);
        Object id = entityAccessStrategy.getIdentityPropertyReader(classInfo).read(entity);
        return !objectMemo.remembered((Long)id, entity, classInfo);
    }

    // these methods belong on the relationship registry
    public boolean isRegisteredRelationship(MappedRelationship relationship) {
        return relationshipRegister.contains(relationship);
    }

    public Set<MappedRelationship> mappedRelationships() {
        return relationshipRegister;
    }

    public void registerRelationship(MappedRelationship relationship) {
        if(relationship.getRelationshipId()!=null && relationshipEntityRegister.get(relationship.getRelationshipId())==null) {
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
    }


    // relationshipentity methods
    public Object getRelationshipEntity(Long relationshipId) {
        return relationshipEntityRegister.get(relationshipId);
    }

    public Object registerRelationshipEntity(Object relationshipEntity, Long id) {
        relationshipEntityRegister.putIfAbsent(id, relationshipEntity);
        registerTypes(relationshipEntity.getClass(), relationshipEntity, id);
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
                    //clear(Class.forName(implementingClassName));
                    clear(MetaDataClassLoader.loadClass(implementingClassName));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else {
            PropertyReader identityReader = entityAccessStrategy.getIdentityPropertyReader(classInfo);
            clear(type, identityReader);
        }
    }

    /**
     * Deregister a relationship entity if it has either start or end node equal to the supplied startOrEndEntity
     * @param startOrEndEntity the entity that might be the start or end node of a relationship entity
     */
    private void deregisterDependentRelationshipEntity(Object startOrEndEntity) {
        Iterator<Long> relationshipEntityIdIterator = relationshipEntityRegister.keySet().iterator();
        while (relationshipEntityIdIterator.hasNext()) {
            Long relationshipEntityId = relationshipEntityIdIterator.next();
            Object relationshipEntity = relationshipEntityRegister.get(relationshipEntityId);
            RelationalReader startNodeReader = entityAccessStrategy.getStartNodeReader(metaData.classInfo(relationshipEntity));
            RelationalReader endNodeReader = entityAccessStrategy.getEndNodeReader(metaData.classInfo(relationshipEntity));
            if (startOrEndEntity == startNodeReader.read(relationshipEntity) || startOrEndEntity ==endNodeReader.read(relationshipEntity)) {
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

    /**
     * purges all information about this object from the mapping context
     *
     * @param entity the type whose object references and relationship mappings we want to purge
     */
    public void clear(Object entity) {
        Class<?> type = entity.getClass();
        ClassInfo classInfo = metaData.classInfo(type.getName());
        PropertyReader identityReader = entityAccessStrategy.getIdentityPropertyReader(classInfo);
        purge(entity, identityReader, type);
        getAll(type).remove(entity);
    }

    private void purge(Object entity, PropertyReader identityReader, Class type) {
        Long id = (Long) identityReader.read(entity);
        if (id != null) {
            if (nodeEntityRegister.containsKey(id)) {
                nodeEntityRegister.remove(id);
                // remove all relationship mappings to/from this object
                Iterator<MappedRelationship> mappedRelationshipIterator = mappedRelationships().iterator();
                while (mappedRelationshipIterator.hasNext()) {
                    MappedRelationship mappedRelationship = mappedRelationshipIterator.next();
                    if (mappedRelationship.getStartNodeId() == id || mappedRelationship.getEndNodeId() == id) {
                        mappedRelationshipIterator.remove();
                    }
                }
            }
            if (metaData.isRelationshipEntity(type.getName()) && relationshipEntityRegister.containsKey(id)) {
                relationshipEntityRegister.remove(id);
                RelationalReader startNodeReader = entityAccessStrategy.getStartNodeReader(metaData.classInfo(entity));
                clear(startNodeReader.read(entity));
                RelationalReader endNodeReader = entityAccessStrategy.getEndNodeReader(metaData.classInfo(entity));
                clear(endNodeReader.read(entity));
            }
        }
    }

}
