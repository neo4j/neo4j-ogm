package org.neo4j.ogm.result.adapter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Frantisek Hartman
 */
public class BaseAdapter {

    public Map<String, Object> convertArrayPropertiesToIterable(Map<String, Object> properties) {
        Map<String, Object> props = new HashMap<>();
        for (String k : properties.keySet()) {
            Object v = properties.get(k);
            if (v.getClass().isArray()) {
                props.put(k, AdapterUtils.convertToIterable(v));
            } else {
                props.put(k, v);
            }
        }
        return props;
    }
}
