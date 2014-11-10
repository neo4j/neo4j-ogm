package org.graphaware.graphmodel.neo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeModel {

    Long id;
    String[] labels;
    List<Property<String, Object>> properties;

    public List<Property<String, Object>> getPropertyList() {
        return properties;
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> map = new HashMap<>();
        for (Property<String, Object> property : properties) {
            map.put(property.getKey(), property.getValue());
        }
        return map;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = new ArrayList<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            this.properties.add(new Property<String, Object>(entry.getKey(), entry.getValue()));
        }
    }

    public void setPropertyList(List<Property<String, Object>> properties) {
        this.properties = properties;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public Object property(String key) {
        for (Property property : properties) {
            if (property.getKey().equals(key)) return property.getValue();
        }
        return null;
    }

}