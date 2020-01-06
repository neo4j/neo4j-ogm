/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
