package org.neo4j.ogm.mapper;

import org.neo4j.ogm.metadata.MetaData;

import java.util.*;
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
        getAll(object.getClass()).add(object = objectMap.get(id));
        return object;
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
