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

package org.neo4j.ogm.domain.social;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

/**
 * POJO used to test the direction of an undirected relationship.
 *
 * @author Luanne Misquitta
 */
public class User {

	private Long id;
	private String name;

	@Relationship(type = "FRIEND", direction = "UNDIRECTED")
	private List<User> friends=new ArrayList<>();

	public User() {
	}

	public User(String name) {
		this.name = name;
	}

	public List<User> getFriends() {
		return friends;
	}

	public void setFriends(List<User> friends) {
		this.friends = friends;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void befriend(User user) {
		friends.add(user);
		user.friends.add(this);
	}

	public void unfriend(User user) {
		friends.remove(user);
		user.friends.remove(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof User)) return false;

		User user = (User) o;

		return name != null ? name.equals(user.name) : user.name == null;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}
}
