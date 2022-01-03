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
package org.neo4j.ogm.domain.ingredients;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Luanne Misquitta
 */
@RelationshipEntity(type = "PAIRS_WITH")
public class Pairing {

    Long id;

    @StartNode
    private Ingredient first;
    @EndNode
    private Ingredient second;
    private String affinity;

    public Pairing() {
    }

    public Ingredient getFirst() {
        return first;
    }

    public void setFirst(Ingredient first) {
        this.first = first;
    }

    public Ingredient getSecond() {
        return second;
    }

    public void setSecond(Ingredient second) {
        this.second = second;
    }

    public String getAffinity() {
        return affinity;
    }

    public void setAffinity(String affinity) {
        this.affinity = affinity;
    }

    public Long getId() {
        return id;
    }
}
