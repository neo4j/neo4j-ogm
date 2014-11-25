package org.neo4j.ogm.mapper;

import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.info.ClassInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The MappingContext maintains a map of all the objects created during the hydration
 * of an object map (domain hierarchy). The MappingContext lifetime is concurrent
 * with a session lifetime.
 */
public class MappingContext {

    private final ConcurrentMap<Long, Object>          objectMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Set<Object>> typeMap = new ConcurrentHashMap<>();
    private final ObjectMemo objectMemo = new ObjectMemo();

    public Object get(Long id) {
        return  objectMap.get(id);
    }

    public Object register(Object object, Long id) {
        objectMap.putIfAbsent(id, object);
        getObjects(object.getClass()).add(object = objectMap.get(id));
        return object;
    }


    public Object getRoot(Class<?> type ) throws Exception {
        Set<Object> roots = typeMap.get(type);
        if (roots == null || roots.isEmpty()) {
            throw new MappingException("FATAL: Could not return class of type " + type.getSimpleName());
        }
        return roots.iterator().next();
    }

    public Set<Object> getObjects(Class<?> type) {
        Set<Object> objectList = typeMap.get(type);
        if (objectList == null) {
            typeMap.putIfAbsent(type, Collections.synchronizedSet(objectList = new HashSet<>()));
        }
        return objectList;
    }

    public void remember(Object object, ClassInfo classInfo) {
        objectMemo.remember(object, classInfo);
    }

    public boolean isDirty(Object toPersist, ClassInfo classInfo) {
        return !objectMemo.remembered(toPersist, classInfo);
    }
}
