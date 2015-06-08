package org.neo4j.ogm.defects;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.ogm.domain.linkedlist.Item;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

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
    public Neo4jIntegrationTestRule neo4jRule = new Neo4jIntegrationTestRule();

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory("org.neo4j.ogm.domain.linkedlist").openSession(neo4jRule.url());
    }

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

