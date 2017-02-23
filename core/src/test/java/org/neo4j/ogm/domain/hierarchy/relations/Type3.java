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

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
public class Type3 extends BaseEntity{

	@Relationship(type = "TYPE_TO", direction = "OUTGOING")
	private Set<Type3> type3Out = new HashSet<>();

	@Relationship(type = "TYPE_TO", direction = "INCOMING")
	private Set<Type3> type3In = new HashSet<>();


	public Set<Type3> getType3Out() {
		return type3Out;
	}

	public void setType3Out(Set<Type3> type3Out) {
		this.type3Out = type3Out;
	}

	public Set<Type3> getType3In() {
		return type3In;
	}

	public void setType3In(Set<Type3> type3In) {
		this.type3In = type3In;
	}
}
