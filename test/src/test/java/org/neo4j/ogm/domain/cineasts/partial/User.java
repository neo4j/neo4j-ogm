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
package org.neo4j.ogm.domain.cineasts.partial;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;

/**
 * @author Vince Bickers
 */
public class User {

    Long id;
    String login;
    String name;
    String password;

    @Relationship(type = "RATED")
    Set<Rating> ratings;

    Set<User> friends;

    @Convert(SecurityRoleConverter.class)
    SecurityRole[] securityRoles;
    @Convert(TitleConverter.class)
    List<Title> titles;

    @Convert(URLArrayConverter.class)
    URL[] urls;

    String[] nicknames;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(Set<Rating> ratings) {
        this.ratings = ratings;
    }

    public Set<User> getFriends() {
        return friends;
    }

    public void setFriends(Set<User> friends) {
        this.friends = friends;
    }

    public SecurityRole[] getSecurityRoles() {
        return securityRoles;
    }

    public void setSecurityRoles(SecurityRole[] securityRoles) {
        this.securityRoles = securityRoles;
    }

    public List<Title> getTitles() {
        return titles;
    }

    public void setTitles(List<Title> titles) {
        this.titles = titles;
    }

    public URL[] getUrls() {
        return urls;
    }

    public void setUrls(URL[] urls) {
        this.urls = urls;
    }

    public String[] getNicknames() {
        return nicknames;
    }

    public void setNicknames(String[] nicknames) {
        this.nicknames = nicknames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;
        if (name == null || user.getName() == null) {
            return false;
        }

        if (!name.equals(user.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (name != null) ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "User:" + name;
    }
}
