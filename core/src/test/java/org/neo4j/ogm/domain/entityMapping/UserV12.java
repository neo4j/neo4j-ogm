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

package org.neo4j.ogm.domain.entityMapping;

import org.neo4j.ogm.annotation.Relationship;

/**
 * One incoming and one outgoing relationship of the same type. Incoming field and methods annotated. Outgoing methods annotated, field not annotated.
 * @author Luanne Misquitta
 */
public class UserV12 extends Entity {

	private UserV12 friend;

	@Relationship(type = "LIKES", direction = "INCOMING")
	private UserV12 friendOf;

	public UserV12() {
	}

	@Relationship(type = "LIKES")
	public UserV12 getFriend() {
		return friend;
	}

	@Relationship(type = "LIKES")
	public void setFriend(UserV12 friend) {
		this.friend = friend;
	}

	@Relationship(type = "LIKES", direction = "INCOMING")
	public UserV12 getFriendOf() {
		return friendOf;
	}

	@Relationship(type = "LIKES", direction = "INCOMING")
	public void setFriendOf(UserV12 friendOf) {
		this.friendOf = friendOf;
	}
}
