/*
 * Copyright (c) 2002-2025 "Neo4j,"
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
package org.neo4j.ogm.persistence.model.gh1134;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity
public class RelEntity {

    public Long id;

    @StartNode
    private NodeWithSomewhatInvalidRelationshipDeclaration start;

    @EndNode
    private NodeWithSomewhatInvalidRelationshipDeclaration end;

    public RelEntity(
        NodeWithSomewhatInvalidRelationshipDeclaration start, NodeWithSomewhatInvalidRelationshipDeclaration end) {
        this.end = end;
        this.start = start;
    }
}
