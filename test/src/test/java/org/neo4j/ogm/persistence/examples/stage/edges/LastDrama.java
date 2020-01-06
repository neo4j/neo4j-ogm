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
package org.neo4j.ogm.persistence.examples.stage.edges;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.persistence.examples.stage.nodes.Drama;
import org.neo4j.ogm.persistence.examples.stage.nodes.StageActor;

@RelationshipEntity(type = "LAST_APPEARANCE")
public class LastDrama extends BaseEdge<StageActor, Drama> {

    public LastDrama() {
        super();
    }

    public LastDrama(StageActor start, Drama end, String title) {
        super(start, end, title);
    }
}
