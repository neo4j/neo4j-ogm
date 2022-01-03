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
package org.neo4j.ogm.domain.entityMapping;

import org.neo4j.ogm.annotation.Relationship;

/**
 * Annotated incoming field with annotated getters and setters. Relationship type derived from property name
 *
 * @author Luanne Misquitta
 */
public class UserV1 extends Entity {

    @Relationship(type = "KNOWN_BY", direction = Relationship.Direction.INCOMING)
    private UserV1 knownBy;

    public UserV1() {
    }

    public UserV1 getKnownBy() {
        return knownBy;
    }

    public void setKnownBy(UserV1 knownBy) {
        this.knownBy = knownBy;
    }
}
