package org.neo4j.ogm.driver.config;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vince
 */
public class DriverConfig {

    private Map<String, Object> config = new HashMap();

    public void setConfig(String key, Object value) {
        config.put(key, value);
    }

    public Object getConfig(String key) {
        return config.get(key);
    }
}
