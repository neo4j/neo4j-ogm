/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.response.model;

import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.model.Property;

/**
 * @author Michal Bachman
 * @author Mark Angrish
 */
public class NodeModel implements Node {

    private Long id;
    private Property<String, Long> version;
    private String[] labels;
    private String[] removedLabels;
    private List<Property<String, Object>> properties = new ArrayList<>();
    private String primaryIndex;

    @Override
    public List<Property<String, Object>> getPropertyList() {
        return properties;
    }

    @Override
    public String getPrimaryIndex() {
        return primaryIndex;
    }

    public void setPrimaryIndex(String primaryIndex) {
        this.primaryIndex = primaryIndex;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = new ArrayList<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            this.properties.add(new PropertyModel<>(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Property<String, Long> getVersion() {
        return version;
    }

    public String[] getLabels() {
        return labels;
    }

    @Override
    public String[] getRemovedLabels() {
        return removedLabels;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setVersion(Property<String, Long> version) {
        this.version = version;
    }

    @Override
    public boolean hasVersionProperty() {
        return version != null;
    }

    public void setLabels(String[] labels) {
        Arrays.sort(labels);
        this.labels = labels;
    }

    public void removeLabels(String[] labels) {
        Arrays.sort(labels);
        this.removedLabels = labels;
    }

    public Object property(String key) {
        for (Property property : properties) {
            if (property.getKey().equals(key))
                return property.getValue();
        }
        return null;
    }

    public String labelSignature() {
        ArrayList<String> allLabels = new ArrayList<>();
        Collections.addAll(allLabels, labels);

        if (removedLabels != null) {
            allLabels.add("_SEPARATOR_");
            Collections.addAll(allLabels, removedLabels);
        }

        return allLabels.stream().collect(joining(","));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        NodeModel nodeModel = (NodeModel) o;

        return id.equals(nodeModel.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
