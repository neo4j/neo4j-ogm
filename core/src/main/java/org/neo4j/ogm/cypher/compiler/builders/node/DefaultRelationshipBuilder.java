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

package org.neo4j.ogm.cypher.compiler.builders.node;

import java.util.Map;

import org.neo4j.ogm.cypher.compiler.RelationshipBuilder;
import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.response.model.PropertyModel;
import org.neo4j.ogm.response.model.RelationshipModel;
import org.neo4j.ogm.utils.EntityUtils;

/**
 * @author Luanne Misquitta
 */
public class DefaultRelationshipBuilder implements RelationshipBuilder {

    RelationshipModel relationship = new RelationshipModel();
    private String direction;
    private boolean singleton = true; // will be false if the relationship can be mapped multiple times between two instances
    private boolean bidirectional = false;
    private boolean relationshipEntity = false;

    public DefaultRelationshipBuilder(String type, boolean bidirectional) {
        relationship.setType(type);
        relationship.setId(EntityUtils.nextRef());
        this.bidirectional = bidirectional;
    }

    public DefaultRelationshipBuilder(String type, Long relationshipId) {
        if (relationshipId == null) {
            relationshipId = EntityUtils.nextRef();
        }
        relationship.setId(relationshipId);
        relationship.setType(type);
    }

    @Override
    public Long reference() {
        return relationship.getId();
    }

    @Override
    public void setType(String type) {
        relationship.setType(type);
    }

    @Override
    public void addProperty(String key, Object value) {
        relationship.getPropertyList().add(new PropertyModel<>(key, value));
    }

    @Override
    public void addProperties(Map<String, ?> properties) {
        for (String key : properties.keySet()) {
            addProperty(key, properties.get(key));
        }
    }

    @Override
    public void relate(Long startNodeId, Long endNodeId) {
        relationship.setStartNode(startNodeId);
        relationship.setEndNode(endNodeId);
    }

    @Override
    public String type() {
        return relationship.getType();
    }

    @Override
    public boolean hasDirection(String direction) {
        return this.direction != null && this.direction.equals(direction);
    }

    @Override
    public boolean isSingleton() {
        return singleton;
    }

    @Override
    public boolean isNew() {
        return true;
    }

    @Override
    public void setSingleton(boolean b) {
        this.singleton = b;
    }

    @Override
    public boolean isRelationshipEntity() {
        return relationshipEntity;
    }

    @Override
    public void setRelationshipEntity(boolean relationshipEntity) {
        this.relationshipEntity = relationshipEntity;
    }

    @Override
    public RelationshipBuilder direction(String direction) {
        this.direction = direction;
        return this;
    }

    @Override
    public boolean isBidirectional() {
        return bidirectional;
    }

    @Override
    public Edge edge() {
        return relationship;
    }

    @Override
    public void setReference(Long reference) {
        relationship.setId(reference);
    }

    @Override
    public void setPrimaryIdName(String primaryIdName) {
        relationship.setPrimaryIdName(primaryIdName);
    }
}
