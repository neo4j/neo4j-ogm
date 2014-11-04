package org.neo4j.ogm.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The MappingContext maintains a map of all the objects created during the hydration
 * of an object map (domain hierarchy).
 */
public class MappingContext {

    private final ConcurrentMap<Long, Object>           objectMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, List<Object>> typeMap = new ConcurrentHashMap<>();

    public Object get(Long id) {
        return  objectMap.get(id);
    }

    public Object register(Object object, Long id) {
        objectMap.putIfAbsent(id, object);
        return objectMap.get(id);
    }

    public Object getRoot(Class<?> type ) throws Exception {
        List<Object> roots = typeMap.get(type);
        return roots.get(roots.size()-1);
    }

    public void evict(Class<?> type) {
        typeMap.remove(type);
    }

    public List<Object> getObjects(Class<?> type) {
        List<Object> objectList = typeMap.get(type);
        if (objectList == null) {
            typeMap.putIfAbsent(type, Collections.synchronizedList(new ArrayList<>()));
            objectList = typeMap.get(type);
        }
        return objectList;
    }

}
