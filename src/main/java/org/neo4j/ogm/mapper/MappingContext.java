package org.neo4j.ogm.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The MappingContext maintains a map of all the objects created during the hydration
 * of an object map (domain hierarchy).
 *
 * TODO ensure this is threadsafe
 */
public class MappingContext {

    private final Map<Long, Object> objectMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Object>> typeMap = new ConcurrentHashMap<>();

    private Class<?> root;

    public Object get(Long id) {
        return  objectMap.get(id);
    }

    public void register(Object object, Long id) throws Exception {
        objectMap.put(id, object);
    }

    public void setRoot(Class<?> root) {
        this.root = root;
        typeMap.clear();
    }

    public Object getRoot() throws Exception {
        List<Object> roots = typeMap.get(root);
        return roots.get(roots.size()-1);
    }

    public void evict(Class<?> type) {
        typeMap.remove(type);
    }

    public List<Object> getObjects(Class<?> type) {
        List<Object> objectList = typeMap.get(type);
        if (objectList == null) {
            typeMap.put(type, objectList = new ArrayList<>());
        }
        return objectList;
    }

}
