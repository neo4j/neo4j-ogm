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

package org.neo4j.ogm.domain.tree;

import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Luanne Misquitta
 */
public class Entity {
	private Long id;
	private String name;

	@Relationship(type = "REL", direction = "OUTGOING")
	private Entity parent;

	@Relationship(type = "REL", direction = "INCOMING")
	private Set<Entity> children = new HashSet<>();

	public Entity() {}

	public Entity(String name) {
		this.name = name;
	}

	public Entity setParent(Entity parent) {
		parent.children.add(this);
		this.parent = parent;
		return this;
	}

	public Long getId() {
		return id;
	}

	@Relationship(type = "REL", direction = "INCOMING")
	public Set<Entity> getChildren() {
		return children;
	}

	@Relationship(type = "REL", direction = "INCOMING")
	public void setChildren(Set<Entity> children) {
		this.children = children;
	}

	public Entity getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}
}
