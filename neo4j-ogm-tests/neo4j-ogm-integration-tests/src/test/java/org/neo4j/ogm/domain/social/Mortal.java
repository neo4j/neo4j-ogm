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
package org.neo4j.ogm.domain.social;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * POJO to test the direction of an incoming relationships.
 *
 * @author Luanne Misquitta
 */
public class Mortal {

    private Long id;
    private String name;

    @Relationship(type = "KNOWN_BY", direction = Relationship.Direction.INCOMING)
    private Set<Mortal> knownBy = new HashSet<>();

    public Mortal() {
    }

    public Mortal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Mortal> getKnownBy() {
        return knownBy;
    }

    public void setKnownBy(Set<Mortal> knownBy) {
        this.knownBy = knownBy;
    }

    /*
     * this method is here to prove that only true JavaBean style setters/getters are invoked by the mapper
     * see ClassInfo.findSetters() and ClassInfo.findGetters()
     */
    public void addKnownBy(Mortal mortal) {
        knownBy.add(mortal);
    }
}
