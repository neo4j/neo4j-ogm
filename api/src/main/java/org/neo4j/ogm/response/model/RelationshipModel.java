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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.model.Property;

/**
 * @author Michal Bachman
 * @author Michael J. Simons
 */
public class RelationshipModel extends AbstractPropertyContainer implements Edge {

    private Long id;
    private Property<String, Long> version;
    private String type;
    private Long startNode;
    private Long endNode;
    private List<Property<String, Object>> properties = new ArrayList<>();
    private String primaryIdName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Property<String, Long> getVersion() {
        return version;
    }

    public void setVersion(Property<String, Long> version) {
        this.version = version;
    }

    @Override
    public boolean hasVersionProperty() {
        return version != null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getStartNode() {
        return startNode;
    }

    public void setStartNode(Long startNode) {
        this.startNode = startNode;
    }

    public Long getEndNode() {
        return endNode;
    }

    public void setEndNode(Long endNode) {
        this.endNode = endNode;
    }

    public List<Property<String, Object>> getPropertyList() {
        return properties;
    }

    @Override
    public String getPrimaryIdName() {
        return primaryIdName;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = new ArrayList<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            this.properties.add(new PropertyModel<>(entry.getKey(), entry.getValue()));
        }
    }

    public void setPrimaryIdName(String primaryIdPropertyName) {
        this.primaryIdName = primaryIdPropertyName;
    }

    @Override
    public String toString() {
        return String.format("(%d)-[%s]->(%d)", this.startNode, this.type, this.endNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RelationshipModel that = (RelationshipModel) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
