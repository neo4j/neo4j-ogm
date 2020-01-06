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

package org.neo4j.ogm.domain.policy;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Mark Angrish
 * @author Luanne Misquitta
 */
public class Person extends DomainObject {

    private Set<Policy> influenced = new HashSet<>();

    @Relationship(type = "WRITES_POLICY")
    private Set<Policy> written = new HashSet<>();

    @JsonIgnore
    @Relationship(type = "AUTHORIZED_POLICY")
    private Policy authorized;

    public Person() {
    }

    public Person(String name) {
        setName(name);
    }

    public Set<Policy> getInfluenced() {
        return influenced;
    }

    public void setInfluenced(Set<Policy> influenced) {
        this.influenced = influenced;
    }

    public Set<Policy> getWritten() {
        return written;
    }

    public void setWritten(Set<Policy> written) {
        this.written = written;
    }

    public Policy getAuthorized() {
        return authorized;
    }

    public void setAuthorized(Policy authorized) {
        this.authorized = authorized;
    }
}
