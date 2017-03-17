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

package org.neo4j.ogm.domain.entityMapping.iterables;

import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

/**
 * One outgoing and one incoming relationship of the same type. Incoming iterable field and methods annotated. Outgoing iterable field annotated , methods not annotated.
 *
 * @author Luanne Misquitta
 */
public class UserV11 extends Entity {

    @Relationship(type = "LIKES", direction = "OUTGOING")
    private Set<UserV11> friend;

    @Relationship(type = "LIKES", direction = "INCOMING")
    private Set<UserV11> friendOf;

    public UserV11() {
    }

    public Set<UserV11> getFriend() {
        return friend;
    }

    public void setFriend(Set<UserV11> friend) {
        this.friend = friend;
    }

    public Set<UserV11> getFriendOf() {
        return friendOf;
    }

    public void setFriendOf(Set<UserV11> friendOf) {
        this.friendOf = friendOf;
    }
}
