/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
import org.neo4j.server.rest.AutoIndexWithNonDefaultConfigurationThroughRESTAPIIT;

/**
 * @author Luanne Misquitta
 */
public class SocialUser {

    @Relationship(type = "HAS_AS_FRIEND", direction = Relationship.Direction.OUTGOING)
    Set<SocialUser> friends;
    @Relationship(type = "FOLLOWING", direction = Relationship.Direction.OUTGOING)
    Set<SocialUser> following;
    @Relationship(type = "IS_FOLLOWED_BY", direction = Relationship.Direction.INCOMING)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SocialUser that = (SocialUser) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (friends != null ? !friends.equals(that.friends) : that.friends != null) {
            return false;
        }
        if (following != null ? !following.equals(that.following) : that.following != null) {
            return false;
        }
        return !(followers != null ? !followers.equals(that.followers) : that.followers != null);
    }

    @Override
    public int hashCode() {
        int result = 31 + (name != null ? name.hashCode() : 0);
        result = 31 * result + (friends != null ? friends.hashCode() : 0);
        result = 31 * result + (following != null ? following.hashCode() : 0);
        result = 31 * result + (followers != null ? followers.hashCode() : 0);
        return result;
    }
}
