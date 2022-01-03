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
package org.neo4j.ogm.domain.hierarchy.relations;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
public class Type3 extends BaseEntity {

    @Relationship(type = "TYPE_TO", direction = Relationship.Direction.OUTGOING)
    private Set<Type3> type3Out = new HashSet<>();

    @Relationship(type = "TYPE_TO", direction = Relationship.Direction.INCOMING)
    private Set<Type3> type3In = new HashSet<>();

    public Set<Type3> getType3Out() {
        return type3Out;
    }

    public void setType3Out(Set<Type3> type3Out) {
        this.type3Out = type3Out;
    }

    public Set<Type3> getType3In() {
        return type3In;
    }

    public void setType3In(Set<Type3> type3In) {
        this.type3In = type3In;
    }
}
