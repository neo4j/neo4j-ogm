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

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
public class Entity implements Comparable{
	private Long id;
	private String name;

	@Relationship(type = "REL", direction = "OUTGOING")
	private Entity parent;

	@Relationship(type = "REL", direction = "INCOMING")
	private SortedSet<Entity> children = new TreeSet<>();

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
	public void setChildren(SortedSet<Entity> children) {
		this.children = children;
	}

	public Entity getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}

	@Override
	public int compareTo(Object o) {
		Entity that = (Entity)o;
		if (this.name != null) {
			if (that.name != null) {
				return this.name.compareTo(that.name);
			}
			else {
				return 1;
			}
		}
		return -1;
	}
}
