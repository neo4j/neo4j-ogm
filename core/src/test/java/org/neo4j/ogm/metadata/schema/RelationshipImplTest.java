/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.metadata.schema;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.ogm.annotation.Relationship.*;

/**
 * @author Frantisek Hartman
 */
public class RelationshipImplTest {

    private NodeImpl start;
    private NodeImpl end;
    private Relationship outgoing;
    private Relationship incoming;
    private Relationship undirected;

    @Before
    public void setUp() throws Exception {
        start = new NodeImpl("Person", Collections.singleton("Person"));
        end = new NodeImpl("Person", Collections.singleton("Person"));

        outgoing = new RelationshipImpl("FRIEND_OF", OUTGOING, start, end);
        incoming = new RelationshipImpl("FRIEND_OF", INCOMING, start, end);
        undirected = new RelationshipImpl("FRIEND_OF", UNDIRECTED, start, end);
    }

    @Test
    public void givenUndirectedRelationship_whenDirection_thenReturnUndirected() throws Exception {
        assertThat(undirected.direction(start)).isEqualTo(UNDIRECTED);
        assertThat(undirected.direction(end)).isEqualTo(UNDIRECTED);
    }

    @Test
    public void givenOutgoingRelationship_whenDirectionStart_thenReturnOutgoing() throws Exception {
        assertThat(outgoing.direction(start)).isEqualTo(OUTGOING);
    }

    @Test
    public void givenOutgoingRelationship_whenDirectionEnd_thenReturnIncoming() throws Exception {
        assertThat(outgoing.direction(end)).isEqualTo(INCOMING);
    }

    @Test
    public void givenIncomingRelationship_whenDirectionStart_thenReturnIncoming() throws Exception {
        assertThat(incoming.direction(start)).isEqualTo(INCOMING);
    }

    @Test
    public void givenIncomingRelationship_whenDirectionEnd_thenReturnOutgoing() throws Exception {
        assertThat(incoming.direction(end)).isEqualTo(OUTGOING);
    }
}
