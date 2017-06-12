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

package org.neo4j.ogm.persistence.examples.linkedlist;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.linkedlist.Item;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Vince Bickers
 * @see DATAGRAPH-636
 * <p/>
 * Support two relationships of the same type and but different directions
 * <p/>
 * The canonical example is a linked list backed by a single edge in the graph model that we
 * want to use as a doubly-linked list in the domain model:
 * <p/>
 * <pre>
 * <code>
 * class Item {
 *      @{literal @}Relationship(type='NEXT', direction=Relationship.INCOMING)
 *      Item previous;
 *      @{literal @}Relationship(type='NEXT', direction=Relationship.OUTGOING)
 *      Item next;
 * }
 * </code>
 * </pre>
 */
public class BidirectionalMappingTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.linkedlist");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
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

    @Test
    public void shouldHandleSelfReferencingObjectOnRollback() {

        Item item = new Item();
        item.next = item;
        item.previous = item;

        try (Transaction tx = session.beginTransaction()) {

            session.save(item);

            session.deleteAll(Item.class);
        }
    }
}

