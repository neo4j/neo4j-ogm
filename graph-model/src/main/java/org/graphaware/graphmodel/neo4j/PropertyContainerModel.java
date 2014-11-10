package org.graphaware.graphmodel.neo4j;

import java.util.Map;

public interface PropertyContainerModel {

    Map<String, Object> getProperties();

    void setProperties(Map<String, Object> properties);

    void setProperty(String key, Object value);

    Object getProperty(String key);

    void removeProperty(String key);
}
