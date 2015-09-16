package org.neo4j.ogm.driver.config;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vince
 */
public class DriverConfig {

    private Map<String, String> config = new HashMap();

    public String getConfig(String key) {
        return config.get(key);
    }

    public void setConfig(String key, String value) {
        config.put(key, value);
    }
}
