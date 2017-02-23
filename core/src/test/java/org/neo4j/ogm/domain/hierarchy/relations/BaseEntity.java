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
package org.neo4j.ogm.domain.hierarchy.relations;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
@NodeEntity(label = "BaseType")
public abstract class BaseEntity {
	@GraphId
	private Long graphId;
	public String name;

	@Relationship(type = "RELATED_TO", direction = "OUTGOING")
	private List<BaseEntity> outgoing = new ArrayList<>();
	@Relationship(type = "RELATED_TO", direction = "INCOMING")
	private List<BaseEntity> incoming = new ArrayList<>();

	public Long getGraphId() {
		return graphId;
	}

	public List<BaseEntity> getOutgoing() {
		return outgoing;
	}

	public void setOutgoing(List<BaseEntity> outgoing) {
		this.outgoing = outgoing;
	}

	public List<BaseEntity> getIncoming() {
		return incoming;
	}

	public void setIncoming(List<BaseEntity> incoming) {
		this.incoming = incoming;
	}

	public void addIncoming(BaseEntity related) {
		incoming.add(related);
		related.getOutgoing().add(this);
	}
}
