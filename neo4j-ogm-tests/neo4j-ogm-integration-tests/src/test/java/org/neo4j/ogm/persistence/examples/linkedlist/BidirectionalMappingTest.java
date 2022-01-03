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
package org.neo4j.ogm.persistence.examples.linkedlist;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.linkedlist.Item;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
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
 *      @{literal @}Relationship(type='NEXT', direction=Relationship.Direction.INCOMING)
 *      Item previous;
 *      @{literal @}Relationship(type='NEXT', direction=Relationship.Direction.OUTGOING)
 *      Item next;
 * }
 * </code>
 * </pre>
 */
public class BidirectionalMappingTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.linkedlist");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
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

        assertThat(first.next.getId()).isEqualTo(second.getId());
        assertThat(first.next.next.getId()).isEqualTo(third.getId());
        assertThat(first.next.next.next).isNull();

        assertThat(first.previous).isNull();
        assertThat(first.next.previous.getId()).isEqualTo(first.getId());
        assertThat(first.next.next.previous.getId()).isEqualTo(second.getId());
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

