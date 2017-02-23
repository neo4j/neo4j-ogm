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

package org.neo4j.ogm.domain.hierarchy.dualRelation;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
@NodeEntity(label = "DataView")
public class DataView  {

	public Long id;
	String name;

	@Relationship(type= "OWNER", direction = Relationship.OUTGOING)
	public Thing owner;

	@Relationship(type= "SHARED_WITH", direction = Relationship.OUTGOING)
	public List<Thing> sharedWith = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Thing getOwner() {
		return owner;
	}

	public void setOwner(Thing owner) {
		this.owner = owner;
	}

	public List<Thing> getSharedWith() {
		return sharedWith;
	}

	public void setSharedWith(List<Thing> sharedWith) {
		this.sharedWith = sharedWith;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
