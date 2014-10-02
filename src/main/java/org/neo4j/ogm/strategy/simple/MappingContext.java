package org.neo4j.ogm.strategy.simple;

import java.util.HashMap;
import java.util.Map;

public class MappingContext {

    private final Map<Long, Object> objectMap = new HashMap<>();

    public Object get(Long id) {
        return  objectMap.get(id);
    }

    public void register(Object object, Long id) throws Exception {
        object.getClass().getMethod("setId", Long.class).invoke(object, id);
        objectMap.put(id, object);
        //System.out.println("registered: " + object.getClass().getSimpleName() + ", id: " + id);
    }

    public void clear() {
        objectMap.clear();
    }

}
