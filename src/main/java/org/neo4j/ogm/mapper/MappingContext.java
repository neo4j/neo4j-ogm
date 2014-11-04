package org.neo4j.ogm.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The MappingContext maintains a map of all the objects created during the hydration
 * of an object map (domain hierarchy).
 *
 * TODO this is not threadsafe
 */
public class MappingContext {

    private final Map<Long, Object> objectMap = new HashMap<>();
    private final Map<Class<?>, List<Object>> typeMap = new HashMap<>();
    private Class<?> root;

    public Object get(Long id) {
        return  objectMap.get(id);
    }

    public void register(Object object, Long id) throws Exception {
        // @FIXME this should be done elsewhere, we don't know if this is the identity field or not.
        object.getClass().getMethod("setId", Long.class).invoke(object, id);
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

    @Deprecated
    // use setRoot(...)
    public void clear() {
        objectMap.clear();
    }

    public List<Object> getObjects(Class<?> type) {
        List<Object> objectList = typeMap.get(type);
        if (objectList == null) {
            typeMap.put(type, objectList = new ArrayList<>());
        }
        return objectList;
    }


}
