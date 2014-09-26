package org.neo4j.ogm.strategy.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingContext {

    private final Map<Long, Object> objectMap = new HashMap<>();
    private final Map<Class, List<Object>> typeMap = new HashMap<>();

    private final Class root;

    public MappingContext(Class root) {
        this.root = root;
    }

    public Object root() throws Exception {
        return typeMap.get(root).get(0);
    }

    public void evict(Class type) {
        typeMap.remove(type);
    }

    public List<Object> get(Class clazz) {
        return typeMap.get(clazz);
    }

    public Object get(Long id) {
        return  objectMap.get(id);
    }

    public void register(Object object, Long id) throws Exception {
        object.getClass().getMethod("setId", Long.class).invoke(object, id);
        objectMap.put(id, object);
        List<Object> objectList = typeMap.get(object.getClass());
        if (objectList == null) {
            objectList = new ArrayList<>();
            typeMap.put(object.getClass(), objectList);
        }
        objectList.add(object);
    }


}
