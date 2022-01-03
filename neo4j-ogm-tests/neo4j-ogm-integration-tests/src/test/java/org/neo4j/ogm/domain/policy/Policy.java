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
package org.neo4j.ogm.domain.policy;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Mark Angrish
 * @author Luanne Misquitta
 */
public class Policy extends DomainObject {

    private Set<Person> influencers = new HashSet<>();

    @Relationship(type = "WRITES_POLICY", direction = Relationship.Direction.INCOMING)
    private Set<Person> writers = new HashSet<>();

    @Relationship(type = "AUTHORIZED_POLICY", direction = Relationship.Direction.INCOMING)
    private Person authorized;

    public Policy() {
    }

    public Policy(String name) {
        setName(name);
    }

    public Set<Person> getInfluencers() {
        return influencers;
    }

    public void setInfluencers(Set<Person> influencers) {
        this.influencers = influencers;
    }

    public Set<Person> getWriters() {
        return writers;
    }

    public void setWriters(Set<Person> writers) {
        this.writers = writers;
    }

    @JsonIgnore
    public Person getAuthorized() {
        return authorized;
    }

    @JsonIgnore
    public void setAuthorized(Person authorized) {
        this.authorized = authorized;
    }
}
