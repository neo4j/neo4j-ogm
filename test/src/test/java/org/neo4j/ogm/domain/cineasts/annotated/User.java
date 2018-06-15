/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.domain.cineasts.annotated;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.UuidStringConverter;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
@NodeEntity
public class User {

    @GraphId
    Long id;

    @Convert(UuidStringConverter.class)
    @Index(unique = true)
    private UUID uuid;

    @Index(unique = true, primary = true)
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

    @Relationship("OWNS")
    List<Pet> pets;

    @Relationship("PLAYS")
    List<Plays> plays;

    public User() {
    }

    public User(String login, String name, String password) {
        this.uuid = UUID.randomUUID();
        this.login = login;
        this.name = name;
        this.password = password;
    }

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

    public List<Pet> getPets() {
        return pets;
    }

    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        User user = (User) o;
        return !(name == null || user.getName() == null) && name.equals(user.name);
    }

    @Override
    public int hashCode() {
        return (name != null) ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "User:" + name;
    }

    public UUID getUuid() {
        return uuid;
    }

    /**
     * Add given users as friends, also adds the opposite direction
     */
    public void addFriends(User... newFriends) {
        if (friends == null) {
            friends = new HashSet<>();
        }

        for (User newFriend : newFriends) {
            friends.add(newFriend);

            if (newFriend.friends == null) {
                newFriend.friends = new HashSet<>();
            }
            newFriend.friends.add(this);
        }
    }
}
