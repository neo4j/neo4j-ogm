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
package org.neo4j.ogm.domain.drink;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.id.UuidStrategy;

/**
 * @author Frantisek Hartman
 */
@RelationshipEntity(type = "OWNS")
public class Owns {

    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    private String acquisitionId;

    @StartNode
    private Manufacturer owner;

    @EndNode
    private Manufacturer ownee;

    private int acquiredYear;

    public Owns() {
    }

    public Owns(Manufacturer owner, Manufacturer ownee, int acquiredYear) {
        this.owner = owner;
        this.ownee = ownee;
        this.acquiredYear = acquiredYear;
    }

    public String getAcquisitionId() {
        return acquisitionId;
    }

    public Manufacturer getOwner() {
        return owner;
    }

    public Manufacturer getOwnee() {
        return ownee;
    }

    public int getAcquiredYear() {
        return acquiredYear;
    }

    public void setAcquiredYear(int acquiredYear) {
        this.acquiredYear = acquiredYear;
    }
}
