/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.response.model;

import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.model.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Michal Bachman
 */
public class NodeModel implements Node {

    private Long id;
    private String[] labels;
    private List<Property<String, Object>> properties = new ArrayList<>();

    public List<Property<String, Object>> getPropertyList() {
        return properties;
    }

//    public Map<String, Object> getProperties() {
//        Map<String, Object> map = new HashMap<>();
//        for (Property<String, Object> property : properties) {
//            map.put(property.getKey(), property.getValue());
//        }
//        return map;
//    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = new ArrayList<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            this.properties.add(new PropertyModel<>(entry.getKey(), entry.getValue()));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeModel nodeModel = (NodeModel) o;

        if (!id.equals(nodeModel.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}