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

    // we need multiple registers whose purpose is obvious from their names:

    // NodeEntityRegister               register of domain entities whose properties are to be stored on Nodes
    // RelationshipEntityRegister       register of domain entities whose properties are to be stored on Relationships
    // RelationshipRegister             register of relationships between NodeEntities (i.e. as they are in the graph)


    private final ConcurrentMap<Long, Object> relationshipEntityRegister = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Object> nodeEntityRegister = new ConcurrentHashMap<>();
    private final Set<MappedRelationship> relationshipRegister = new HashSet<>();

    // in addition we need the following
    // a typeRegister                   register of all entities of a specific type (including supertypes)
    // a
    private final ConcurrentMap<Class<?>, Set<Object>> typeRegister = new ConcurrentHashMap<>();
    private final EntityMemo objectMemo = new EntityMemo();

    private final MetaData metaData;

    public MappingContext(MetaData metaData) {
        this.metaData = metaData;
    }

    // these methods belong on the nodeEntityRegister
    public Object get(Long id) {
        return nodeEntityRegister.get(id);
    }

    public Object registerNodeEntity(Object entity, Long id) {
        nodeEntityRegister.putIfAbsent(id, entity);
        entity = nodeEntityRegister.get(id);
        registerTypes(entity.getClass(), entity);
        return entity;
    }

    private void registerTypes(Class type, Object entity) {
        //System.out.println("registering " + object + " as instance of " + type.getSimpleName());
        getAll(type).add(entity);
        if (type.getSuperclass() != null
                && metaData != null
                && metaData.classInfo(type.getSuperclass().getName()) != null
                && !type.getSuperclass().getName().equals("java.lang.Object")) {
            registerTypes(type.getSuperclass(), entity);
        }
    }

    private void deregisterTypes(Class type, Object entity) {
        //System.out.println("deregistering " + object.getClass().getSimpleName() + " as instance of " + type.getSimpleName());
        getAll(type).remove(entity);
        if (type.getSuperclass() != null
                && metaData != null
                && metaData.classInfo(type.getSuperclass().getName()) != null
                && !type.getSuperclass().getName().equals("java.lang.Object")) {
            deregisterTypes(type.getSuperclass(), entity);
        }
    }

    /**
     * Deregisters an object from the mapping context
     * - removes the object instance from the typeRegister(s)
     * - removes the object id from the nodeEntityRegister
     *
     * @param entity the object to deregister
     * @param id the id of the object in Neo4j
     */
    public void deregister(Object entity, Long id) {
        deregisterTypes(entity.getClass(), entity);
        nodeEntityRegister.remove(id);
    }

    public Set<Object> getAll(Class<?> type) {
        Set<Object> objectList = typeRegister.get(type);
        if (objectList == null) {
            typeRegister.putIfAbsent(type, Collections.synchronizedSet(new HashSet<>()));
            objectList = typeRegister.get(type);
        }
        return objectList;
    }

    // object memoisations
    public void remember(Object entity) {
        objectMemo.remember(entity, metaData.classInfo(entity));
    }

    public boolean isDirty(Object entity) {
        return !objectMemo.remembered(entity, metaData.classInfo(entity));
    }

    // these methods belong on the relationship registry
    public boolean isRegisteredRelationship(MappedRelationship relationship) {
        return relationshipRegister.contains(relationship);
    }

    public Set<MappedRelationship> mappedRelationships() {
        return relationshipRegister;
    }

    public void remember(MappedRelationship relationship) {
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
        return relationshipEntity;
    }

}
