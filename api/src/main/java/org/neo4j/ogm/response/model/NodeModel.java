/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.model.Property;

/**
 * @author Michal Bachman
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class NodeModel extends AbstractPropertyContainer implements Node {

    private final Long id;
    private Property<String, Long> version;
    private String[] labels;
    private List<Property<String, Object>> properties = new ArrayList<>();
    private String primaryIndex;
    /** Flag, if this node has been generated through pattern comprehension. */
    private boolean generatedNode = false;

    /**
     * Those are the previous, dynamic labels if any.
     */
    private Set<String> previousDynamicLabels = Collections.emptySet();

    public NodeModel(Long id) {
        this.id = id;
    }

    public boolean isGeneratedNode() {
        return generatedNode;
    }

    public void setGeneratedNode(boolean generatedNode) {
        this.generatedNode = generatedNode;
    }

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

    public void setVersion(Property<String, Long> version) {
        this.version = version;
    }

    @Override
    public boolean hasVersionProperty() {
        return version != null;
    }

    @Override
    public Set<String> getPreviousDynamicLabels() {
        return Collections.unmodifiableSet(previousDynamicLabels);
    }

    public void setPreviousDynamicLabels(Set<String> previousDynamicLabels) {
        this.previousDynamicLabels = new HashSet<>(previousDynamicLabels);
    }

    public void setLabels(String[] labels) {
        Arrays.sort(labels);
        this.labels = labels;
    }

    public Object property(String key) {
        for (Property property : properties) {
            if (property.getKey().equals(key)) {
                return property.getValue();
            }
        }
        return null;
    }

    @Override
    public String labelSignature() {
        return Stream.concat(
            Arrays.stream(labels),
            Optional.ofNullable(previousDynamicLabels).orElseGet(HashSet::new).stream()
        ).distinct().collect(joining(","));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NodeModel)) {
            return false;
        }
        NodeModel nodeModel = (NodeModel) o;
        return id.equals(nodeModel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
