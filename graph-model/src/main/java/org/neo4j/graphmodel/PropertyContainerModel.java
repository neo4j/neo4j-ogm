package org.neo4j.graphmodel;

import java.util.Map;

public interface PropertyContainerModel {

    Map<String, Object> getProperties();

    void setProperties(Map<String, Object> properties);

    void setProperty(String key, Object value);

    Object getProperty(String key);

    void removeProperty(String key);
}
