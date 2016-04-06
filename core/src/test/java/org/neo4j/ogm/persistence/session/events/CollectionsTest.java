/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.persistence.session.events;

import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.session.event.Event;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author vince
 */
public class CollectionsTest extends EventTest {

    @Test
    public void shouldFireEventsWhenSavingACollection() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        a.setName("newA");
        b.setName("newB");
        c.setName("newC");
        List<Object> saveList = new LinkedList<>();
        saveList.add(a);
        saveList.add(b);
        saveList.add(c);

        session.save(saveList);

        assertEquals(6, eventListenerTest.count());

        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListenerTest.captured(b, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(b, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListenerTest.captured(c, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(c, Event.LIFECYCLE.POST_SAVE));

    }

    @Test
    public void shouldFireEventsWhenDeletingACollection() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        List<Object> deleteList = new LinkedList<>();
        deleteList.add(a);
        deleteList.add(b);
        deleteList.add(c);

        session.delete(deleteList);

        assertEquals(6, eventListenerTest.count());

        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.PRE_DELETE));
        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.POST_DELETE));
        assertTrue(eventListenerTest.captured(b, Event.LIFECYCLE.PRE_DELETE));
        assertTrue(eventListenerTest.captured(b, Event.LIFECYCLE.POST_DELETE));
        assertTrue(eventListenerTest.captured(c, Event.LIFECYCLE.PRE_DELETE));
        assertTrue(eventListenerTest.captured(c, Event.LIFECYCLE.POST_DELETE));

    }

    @Test
    public void shouldFireEventWhenDeletingAllObjectsOfASpecifiedType() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        session.deleteAll(Document.class);

        assertEquals(2, eventListenerTest.count());

        assertTrue(eventListenerTest.captured(Document.class, Event.LIFECYCLE.PRE_DELETE));
        assertTrue(eventListenerTest.captured(Document.class, Event.LIFECYCLE.POST_DELETE));

    }


}
