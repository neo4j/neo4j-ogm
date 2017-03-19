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

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.session.event.Event;

/**
 * @author vince
 */
public class CollectionsTest extends EventTestBaseClass {

    @Test
    public void shouldFireEventsWhenSavingACollection() {

        a.setName("newA");
        b.setName("newB");
        c.setName("newC");

        session.save(Arrays.asList(a, b, c));

        assertEquals(6, eventListener.count());

        assertTrue(eventListener.captured(a, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(a, Event.TYPE.POST_SAVE));
        assertTrue(eventListener.captured(b, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(b, Event.TYPE.POST_SAVE));
        assertTrue(eventListener.captured(c, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(c, Event.TYPE.POST_SAVE));
    }

    @Test
    public void shouldFireEventsWhenDeletingACollection() {

        session.delete(Arrays.asList(a, b, c));

        assertTrue(eventListener.captured(a, Event.TYPE.PRE_DELETE));
        assertTrue(eventListener.captured(a, Event.TYPE.POST_DELETE));
        assertTrue(eventListener.captured(b, Event.TYPE.PRE_DELETE));
        assertTrue(eventListener.captured(b, Event.TYPE.POST_DELETE));
        assertTrue(eventListener.captured(c, Event.TYPE.PRE_DELETE));
        assertTrue(eventListener.captured(c, Event.TYPE.POST_DELETE));

        // even though we haven't updated the folder object, the database
        // has removed the relationships between the folder and the documents, so
        // the folder events must fire
        assertTrue(eventListener.captured(folder, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(folder, Event.TYPE.POST_SAVE));

        assertEquals(8, eventListener.count());
    }

    @Test
    public void shouldFireEventWhenDeletingAllObjectsOfASpecifiedType() {

        session.deleteAll(Document.class);

        assertEquals(2, eventListener.count());

        assertTrue(eventListener.captured(Document.class, Event.TYPE.PRE_DELETE));
        assertTrue(eventListener.captured(Document.class, Event.TYPE.POST_DELETE));
    }

    @Test
    public void shouldFireEventsWhenDeletingObjectsOfDifferentTypes() {

        session.delete(Arrays.asList(folder, knowsJB));

        // object deletes
        assertTrue(eventListener.captured(folder, Event.TYPE.PRE_DELETE));
        assertTrue(eventListener.captured(folder, Event.TYPE.POST_DELETE));
        assertTrue(eventListener.captured(knowsJB, Event.TYPE.PRE_DELETE));
        assertTrue(eventListener.captured(knowsJB, Event.TYPE.POST_DELETE));

        // document updates
        assertTrue(eventListener.captured(a, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(a, Event.TYPE.POST_SAVE));
        assertTrue(eventListener.captured(b, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(b, Event.TYPE.POST_SAVE));
        assertTrue(eventListener.captured(c, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(c, Event.TYPE.POST_SAVE));

        // people updates
        assertTrue(eventListener.captured(jim, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(jim, Event.TYPE.POST_SAVE));
        assertTrue(eventListener.captured(bruce, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(bruce, Event.TYPE.POST_SAVE));

        assertEquals(14, eventListener.count());
    }
}
