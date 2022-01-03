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
package org.neo4j.ogm.domain.cineasts.partial;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
public class Actor {

    public Long id;
    String name;

    @Relationship(type = "ACTS_IN", direction = Relationship.Direction.OUTGOING)
    private List<Role> roles;

    @Relationship(type = "KNOWS", direction = Relationship.Direction.OUTGOING)
    public Set<Knows> knows = new HashSet<>();

    public Actor() {
    }

    public Actor(String name) {
        this.name = name;
    }

    public void addRole(String character, Movie movie) {
        roles.add(new Role(character, this, movie));
    }

    public List<Role> roles() {
        return Collections.unmodifiableList(roles);
    }

    public void removeRole(String character) {

        Iterator<Role> iterator = roles.iterator();
        while (iterator.hasNext()) {
            Role role = iterator.next();
            if (role.played.equals(character)) {
                iterator.remove();
            }
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}


