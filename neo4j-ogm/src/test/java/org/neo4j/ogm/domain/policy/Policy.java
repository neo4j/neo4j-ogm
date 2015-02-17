/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.domain.policy;

import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

public class Policy extends DomainObject {

    private Set<Person> influencers = new HashSet<>();

    @Relationship(type="WRITES_POLICY", direction="INCOMING")
    private Set<Person> writers = new HashSet<>();

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
}
