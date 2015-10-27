/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.core.integration.linkedlist;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.ogm.core.session.Session;
import org.neo4j.ogm.core.session.SessionFactory;
import org.neo4j.ogm.api.service.Components;
import org.neo4j.ogm.testutil.IntegrationTestRule;
import org.neo4j.ogm.domain.linkedlist.Item;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @see DATAGRAPH-636
 *
 * Support two relationships of the same type and but different directions
 *
 * The canonical example is a linked list backed by a single edge in the graph model that we
 * want to use as a doubly-linked list in the domain model:
 *
 * <pre>
 * <code>
 * class Item {
 *
 *      @{literal @}Relationship(type='NEXT', direction=Relationship.INCOMING)
 *      Item previous;
 *
 *      @{literal @}Relationship(type='NEXT', direction=Relationship.OUTGOING)
 *      Item next;
 * }
 * </code>
 * </pre>
 *
 * @author Vince Bickers
 */
public class BidirectionalMappingTest {

    @Rule
    public IntegrationTestRule testServer = new IntegrationTestRule(Components.driver());

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory("org.neo4j.ogm.domain.linkedlist").openSession(testServer.driver());
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void shouldLoadDoublyLinkedList() {

        Item first = new Item();
        Item second = new Item();
        Item third = new Item();

        first.next = second;
        second.next = third;

        session.save(first);

        session.clear();

        first = session.load(Item.class, first.getId(), -1);

        assertEquals(second.getId(), first.next.getId());
        assertEquals(third.getId(), first.next.next.getId());
        assertNull(first.next.next.next);

        assertNull(first.previous);
        assertEquals(first.getId(), first.next.previous.getId());
        assertEquals(second.getId(), first.next.next.previous.getId());

    }

}

