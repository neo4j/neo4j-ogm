package org.neo4j.ogm.mapper;

import java.util.HashMap;
import java.util.Map;

/**
 * The MappingContext maintains a map of all the objects created during the hydration
 * of an object map (domain hierarchy). It is also responsible for
 * registering all new objects by setting the object identifier on them.
 */
public class MappingContext {

    private final Map<Long, Object> objectMap = new HashMap<>();

    public Object get(Long id) {
        return  objectMap.get(id);
    }

    public void register(Object object, Long id) throws Exception {
        // @Deprecated this should be done elsewhere
        object.getClass().getMethod("setId", Long.class).invoke(object, id);
        objectMap.put(id, object);
    }

    public void clear() {
        objectMap.clear();
    }

}
