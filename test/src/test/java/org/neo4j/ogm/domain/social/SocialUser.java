/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.domain.social;

import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
public class SocialUser {

    @Relationship(type = "HAS_AS_FRIEND", direction = Relationship.OUTGOING)
    Set<SocialUser> friends;
    @Relationship(type = "FOLLOWING", direction = Relationship.OUTGOING)
    Set<SocialUser> following;
    @Relationship(type = "IS_FOLLOWED_BY", direction = Relationship.INCOMING)
    Set<SocialUser> followers;
    private Long id;
    private String name;

    public SocialUser() {
    }

    public SocialUser(String name) {
        this.name = name;
    }

    public Set<SocialUser> getFriends() {
        return friends;
    }

    public void setFriends(Set<SocialUser> friends) {
        this.friends = friends;
    }

    public Set<SocialUser> getFollowing() {
        return following;
    }

    public void setFollowing(Set<SocialUser> following) {
        this.following = following;
    }

    public Set<SocialUser> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<SocialUser> followers) {
        this.followers = followers;
    }

    public Long getId() {
        return id;
    }

    /**
     * For test purposes only
     */
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
