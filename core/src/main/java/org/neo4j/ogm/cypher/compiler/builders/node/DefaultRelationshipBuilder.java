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
