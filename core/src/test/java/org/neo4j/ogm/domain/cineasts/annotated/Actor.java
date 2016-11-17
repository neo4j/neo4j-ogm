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

package org.neo4j.ogm.domain.cineasts.annotated;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.UuidStringConverter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
@NodeEntity
public class Actor {

    @GraphId
    private Long id;

    @Convert(UuidStringConverter.class)
    @Index(unique = true, primary = true)
    private UUID uuid;

    @Index
    private String name;

    private Set<Movie> filmography;

    @Relationship(type = "ACTS_IN", direction = "OUTGOING")
    private Set<Role> roles;

    private Set<Nomination> nominations;

    @Relationship(type = "KNOWS", direction = "OUTGOING")
    public Set<Knows> knows = new HashSet<>();

    Actor() {
        // default constructor for OGM
    }

    public Actor(String name) {
        this.uuid = UUID.randomUUID();
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public Role playedIn(Movie movie, String roleName) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        Role role = new Role(movie, this, roleName);
        roles.add(role);
        movie.getRoles().add(role);
        return role;
    }

    public Nomination nominatedFor(Movie movie, String nominationName, int year) {
        if (nominations == null) {
            nominations = new HashSet<>();
        }
        Nomination nomination = new Nomination(movie, this, nominationName, year);
        nominations.add(nomination);
        if (movie.getNominations() == null) {
            movie.setNominations(new HashSet<Nomination>());
        }
        movie.getNominations().add(nomination);
        return nomination;
    }

    public String getName() {
        return name;
    }

    public Set<Knows> getKnows() {
        return knows;
    }

    public Set<Nomination> getNominations() {
        return nominations;
    }

    public void setNominations(Set<Nomination> nominations) {
        this.nominations = nominations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Actor actor = (Actor) o;

        if (name == null || ((Actor) o).getName() == null) return false;

        if (!name.equals(actor.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return "Actor {" +
                "name='" + name + '\'' +
                '}';
    }
}
