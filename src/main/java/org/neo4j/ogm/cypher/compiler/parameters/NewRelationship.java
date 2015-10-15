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
package org.neo4j.ogm.cypher.compiler.parameters;

/**
 * Parameter representing a new relationship to be created between persisted nodes.
 * @author Luanne Misquitta
 */
public class NewRelationship {

	private String startNodeRef;
	private String endNodeRef;
	private Long startNodeId;
	private Long endNodeId;
	private String relRef;

	public NewRelationship() {
	}

	public NewRelationship(String startNodeRef, String endNodeRef, String relRef) {
		this.startNodeRef = startNodeRef;
		this.endNodeRef = endNodeRef;
		this.relRef = relRef;
		startNodeId = Long.parseLong(startNodeRef.substring(1));
		endNodeId = Long.parseLong(endNodeRef.substring(1));
	}

	public Long getStartNodeId() {
		return startNodeId;
	}

	public Long getEndNodeId() {
		return endNodeId;
	}

	public String getRelRef() {
		return relRef;
	}

	public void setStartNodeRef(String startNodeRef) {
		this.startNodeRef = startNodeRef;
	}

	public void setEndNodeRef(String endNodeRef) {
		this.endNodeRef = endNodeRef;
	}

	public void setStartNodeId(Long startNodeId) {
		this.startNodeId = startNodeId;
	}

	public void setEndNodeId(Long endNodeId) {
		this.endNodeId = endNodeId;
	}

	public void setRelRef(String relRef) {
		this.relRef = relRef;
	}
}
