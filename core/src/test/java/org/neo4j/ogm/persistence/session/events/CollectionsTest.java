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

        eventListener = new TestEventListener();
        session.register(eventListener);

        a.setName("newA");
        b.setName("newB");
        c.setName("newC");
        List<Object> saveList = new LinkedList<>();
        saveList.add(a);
        saveList.add(b);
        saveList.add(c);

        session.save(saveList);

        assertEquals(6, eventListener.count());

        assertTrue(eventListener.captured(a, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListener.captured(a, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListener.captured(b, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListener.captured(b, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListener.captured(c, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListener.captured(c, Event.LIFECYCLE.POST_SAVE));

    }

    @Test
    public void shouldFireEventsWhenDeletingACollection() {

        eventListener = new TestEventListener();
        session.register(eventListener);

        List<Object> deleteList = new LinkedList<>();
        deleteList.add(a);
        deleteList.add(b);
        deleteList.add(c);

        session.delete(deleteList);

        assertTrue(eventListener.captured(a, Event.LIFECYCLE.PRE_DELETE));
        assertTrue(eventListener.captured(a, Event.LIFECYCLE.POST_DELETE));
        assertTrue(eventListener.captured(b, Event.LIFECYCLE.PRE_DELETE));
        assertTrue(eventListener.captured(b, Event.LIFECYCLE.POST_DELETE));
        assertTrue(eventListener.captured(c, Event.LIFECYCLE.PRE_DELETE));
        assertTrue(eventListener.captured(c, Event.LIFECYCLE.POST_DELETE));

        // even though we haven't updated the folder object, the database
        // has removed the relationships between the folder and the documents, so
        // the folder events must fire
        assertTrue(eventListener.captured(folder, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListener.captured(folder, Event.LIFECYCLE.POST_SAVE));

        assertEquals(8, eventListener.count());

    }

    @Test
    public void shouldFireEventWhenDeletingAllObjectsOfASpecifiedType() {

        eventListener = new TestEventListener();
        session.register(eventListener);

        session.deleteAll(Document.class);

        assertEquals(2, eventListener.count());

        assertTrue(eventListener.captured(Document.class, Event.LIFECYCLE.PRE_DELETE));
        assertTrue(eventListener.captured(Document.class, Event.LIFECYCLE.POST_DELETE));

    }


}
