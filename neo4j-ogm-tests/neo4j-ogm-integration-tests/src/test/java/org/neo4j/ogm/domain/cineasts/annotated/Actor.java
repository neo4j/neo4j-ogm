/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.domain.cineasts.annotated;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.id.UuidStrategy;
import org.neo4j.ogm.typeconversion.UuidStringConverter;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
@NodeEntity
public class Actor {

    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    @Convert(UuidStringConverter.class)
    private UUID uuid;

    @Index
    private String name;

    private Set<Movie> filmography;

    @Relationship(type = "ACTS_IN", direction = Relationship.Direction.OUTGOING)
    private Set<Role> roles;

    private Set<Nomination> nominations = new HashSet<>();

    @Relationship(type = "KNOWS", direction = Relationship.Direction.OUTGOING)
    public Set<Knows> knows = new HashSet<>();

    Actor() {
        // default constructor for OGM
    }

    public Actor(String name) {
        this.name = name;
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Actor actor = (Actor) o;

        if (name == null || ((Actor) o).getName() == null) {
            return false;
        }

        return name.equals(actor.name);
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
