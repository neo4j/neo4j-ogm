package org.neo4j.ogm.mapper;

import org.neo4j.ogm.metadata.MetaData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The MappingContext maintains a map of all the objects created during the hydration
 * of an object map (domain hierarchy). The MappingContext lifetime is concurrent
 * with a session lifetime.
 */
public class MappingContext {

    private final ConcurrentMap<Long, Object> relationshipEntityMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Object> objectMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Set<Object>> typeMap = new ConcurrentHashMap<>();

    // using these two objects we maintain synchronisation state with the database
    private final Set<MappedRelationship> mappedRelationships = new HashSet<>();
    private final ObjectMemo objectMemo = new ObjectMemo();

    private final MetaData metaData;

    public MappingContext(MetaData metaData) {
        this.metaData = metaData;
    }

    public Object get(Long id) {
        return objectMap.get(id);
    }

    public Object register(Object object, Long id) {
        objectMap.putIfAbsent(id, object);
        object = objectMap.get(id);
        registerTypes(object.getClass(), object);
        return object;
    }

    private void registerTypes(Class type, Object object) {
        //System.out.println("registering " + object + " as instance of " + type.getSimpleName());
        getAll(type).add(object);
        if (type.getSuperclass() != null
                && metaData != null
                && metaData.classInfo(type.getSuperclass().getName()) != null
                && !type.getSuperclass().getName().equals("java.lang.Object")) {
            registerTypes(type.getSuperclass(), object);
        }
    }

    private void deregisterTypes(Class type, Object object) {
        //System.out.println("deregistering " + object.getClass().getSimpleName() + " as instance of " + type.getSimpleName());
        getAll(type).remove(object);
        if (type.getSuperclass() != null
                && metaData != null
                && metaData.classInfo(type.getSuperclass().getName()) != null
                && !type.getSuperclass().getName().equals("java.lang.Object")) {
            deregisterTypes(type.getSuperclass(), object);
        }
    }

    /**
     * Deregisters an object from the mapping context
     * - removes the object instance from the typeMap(s)
     * - removes the object id from the objectMap
     *
     * @param object the object to deregister
     * @param id the id of the object in Neo4j
     */
    public void deregister(Object object, Long id) {
        deregisterTypes(object.getClass(), object);
        objectMap.remove(id);
    }

    public Set<Object> getAll(Class<?> type) {
        Set<Object> objectList = typeMap.get(type);
        if (objectList == null) {
            typeMap.putIfAbsent(type, Collections.synchronizedSet(new HashSet<>()));
            objectList = typeMap.get(type);
        }
        return objectList;
    }

    public void remember(Object object) {
        objectMemo.remember(object, metaData.classInfo(object.getClass().getName()));
    }

    public boolean isDirty(Object toPersist) {
        return !objectMemo.remembered(toPersist, metaData.classInfo(toPersist.getClass().getName()));
    }

    public boolean isRegisteredRelationship(MappedRelationship relationship) {
        return mappedRelationships.contains(relationship);
    }

    public Set<MappedRelationship> mappedRelationships() {
        return mappedRelationships;
    }

    public void remember(MappedRelationship relationship) {
        mappedRelationships.add(relationship);
    }

    public void clear() {
        objectMemo.clear();
        mappedRelationships.clear();
        objectMap.clear();
        typeMap.clear();
        relationshipEntityMap.clear();
    }

    public Object getRelationshipEntity(Long relationshipId) {
        return relationshipEntityMap.get(relationshipId);
    }

    public Object registerRelationship(Object relationshipEntity, Long id) {
        relationshipEntityMap.putIfAbsent(id, relationshipEntity);
        return relationshipEntity;
    }

}
