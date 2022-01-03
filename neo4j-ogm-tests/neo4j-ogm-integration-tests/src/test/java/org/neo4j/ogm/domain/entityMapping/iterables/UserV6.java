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
package org.neo4j.ogm.domain.entityMapping.iterables;

import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * No matching setters or getters but the param type matches and the relationship direction is undirected.
 *
 * @author Luanne Misquitta
 */
public class UserV6 extends Entity {

    @Relationship(type = "KNOWS", direction = Relationship.Direction.UNDIRECTED)
    private Set<UserV6> knowsPeople;

    public Set<UserV6> getKnowsPeople() {
        return knowsPeople;
    }

    public void setKnowsPeople(Set<UserV6> knowsPeople) {
        this.knowsPeople = knowsPeople;
    }
}
