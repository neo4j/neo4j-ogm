/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.compiler.builders.node;

import org.neo4j.ogm.compiler.RelationshipBuilder;
import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.response.model.PropertyModel;
import org.neo4j.ogm.response.model.RelationshipModel;

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
		relationship.setId((long) -System.identityHashCode(relationship));
		this.bidirectional = bidirectional;
	}

	public DefaultRelationshipBuilder(String type, Long relationshipId) {
		if(relationshipId==null) {
			relationshipId = (long) -System.identityHashCode(relationship);
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
}
