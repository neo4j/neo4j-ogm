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
package org.neo4j.ogm.metadata.schema;

import static org.assertj.core.api.Assertions.*;
import static org.neo4j.ogm.annotation.Relationship.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class RelationshipImplTest {

    private NodeImpl start;
    private NodeImpl end;
    private Relationship outgoing;
    private Relationship incoming;
    private Relationship undirected;

    @Before
    public void setUp() {
        start = new NodeImpl("Person");
        end = new NodeImpl("Person");

        outgoing = new RelationshipImpl("FRIEND_OF", Direction.OUTGOING, start, end);
        incoming = new RelationshipImpl("FRIEND_OF", Direction.INCOMING, start, end);
        undirected = new RelationshipImpl("FRIEND_OF", Direction.UNDIRECTED, start, end);
    }

    @Test
    public void givenUndirectedRelationship_whenDirection_thenReturnUndirected() {
        assertThat(undirected.direction(start)).isEqualTo(Direction.UNDIRECTED);
        assertThat(undirected.direction(end)).isEqualTo(Direction.UNDIRECTED);
    }

    @Test
    public void givenOutgoingRelationship_whenDirectionStart_thenReturnOutgoing() {
        assertThat(outgoing.direction(start)).isEqualTo(Direction.OUTGOING);
    }

    @Test
    public void givenOutgoingRelationship_whenDirectionEnd_thenReturnIncoming() {
        assertThat(outgoing.direction(end)).isEqualTo(Direction.INCOMING);
    }

    @Test
    public void givenIncomingRelationship_whenDirectionStart_thenReturnIncoming() {
        assertThat(incoming.direction(start)).isEqualTo(Direction.INCOMING);
    }

    @Test
    public void givenIncomingRelationship_whenDirectionEnd_thenReturnOutgoing() {
        assertThat(incoming.direction(end)).isEqualTo(Direction.OUTGOING);
    }
}
