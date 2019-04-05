/*
 * Copyright (c) 2002-2019 "Neo4j,"
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

import java.util.Optional;

import org.neo4j.ogm.cypher.compiler.RelationshipBuilder;
import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.response.model.PropertyModel;
import org.neo4j.ogm.response.model.RelationshipModel;
import org.neo4j.ogm.utils.EntityUtils;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class DefaultRelationshipBuilder extends AbstractPropertyContainerBuilder<RelationshipBuilder, RelationshipModel>
    implements RelationshipBuilder {

    private String direction;
    private boolean singleton = true; // will be false if the relationship can be mapped multiple times between two instances
    private boolean bidirectional = false;
    private boolean relationshipEntity = false;

    public DefaultRelationshipBuilder(String type, boolean bidirectional) {
        this(type, null);
        this.bidirectional = bidirectional;
    }

    public DefaultRelationshipBuilder(String type, Long relationshipId) {
        super(new RelationshipModel());
        super.targetContainer.setId(Optional.ofNullable(relationshipId).orElseGet(EntityUtils::nextRef));
        super.targetContainer.setType(type);
    }

    @Override
    public Long reference() {
        return super.targetContainer.getId();
    }

    @Override
    public void setType(String type) {
        super.targetContainer.setType(type);
    }

    @Override
    public RelationshipBuilder addProperty(String key, Object value) {
        super.targetContainer.getPropertyList().add(new PropertyModel<>(key, value));
        return this;
    }

    @Override
    public void relate(Long startNodeId, Long endNodeId) {
        super.targetContainer.setStartNode(startNodeId);
        super.targetContainer.setEndNode(endNodeId);
    }

    @Override
    public String type() {
        return super.targetContainer.getType();
    }

    @Override
    public boolean hasDirection(String possibleDirection) {
        return this.direction != null && this.direction.equals(possibleDirection);
    }

    @Override
    public boolean isSingleton() {
        return singleton;
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
    public RelationshipBuilder direction(String newDirection) {
        this.direction = newDirection;
        return this;
    }

    @Override
    public boolean isBidirectional() {
        return bidirectional;
    }

    @Override
    public Edge edge() {
        return super.targetContainer;
    }

    @Override
    public void setReference(Long reference) {
        super.targetContainer.setId(reference);
    }

    @Override
    public void setPrimaryIdName(String primaryIdName) {
        super.targetContainer.setPrimaryIdName(primaryIdName);
    }

    @Override
    public RelationshipBuilder setVersionProperty(String name, Long version) {
        super.targetContainer.setVersion(new PropertyModel<>(name, version));
        return this;
    }
}
