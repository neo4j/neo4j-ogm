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
package org.neo4j.ogm.domain.spies;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
@NodeEntity
public class Spy {

    private Long id;
    private String name;

    @Relationship(type = "SPY", direction = Relationship.Direction.OUTGOING)
    private Target spiesOn;

    @Relationship(type = "SPY", direction = Relationship.Direction.INCOMING)
    private Target spiedOnBy;

    public Spy() {
    }

    public Spy(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Target getSpiesOn() {
        return spiesOn;
    }

    public void setSpiesOn(Target spiesOn) {
        this.spiesOn = spiesOn;
    }

    public Target getSpiedOnBy() {
        return spiedOnBy;
    }

    public void setSpiedOnBy(Target spiedOnBy) {
        this.spiedOnBy = spiedOnBy;
    }
}
