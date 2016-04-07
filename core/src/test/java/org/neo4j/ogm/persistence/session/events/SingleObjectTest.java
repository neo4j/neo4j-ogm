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
import org.neo4j.ogm.domain.cineasts.annotated.Knows;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.event.Event;

import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author vince
 */
public class SingleObjectTest extends EventTest {

    @Test
    public void shouldNotFireEventsIfObjectHasNotChanged() {

        eventListener = new TestEventListener();
        session.register(eventListener);

        session.save(folder);
        assertEquals(0, eventListener.count());
    }

    @Test
    public void shouldNotFireEventsOnLoad() {

        this.eventListener = new TestEventListener();
        session.register(eventListener);

        session.load(Folder.class, folder.getId());
        assertEquals(0, eventListener.count());
    }

    @Test
    public void shouldFireEventsWhenCreatingNewEntity() {

        eventListener = new TestEventListener();
        session.register(eventListener);

        Document e = new Document();
        e.setName("e");
        session.save(e);

        assertEquals(2, eventListener.count());

        assertTrue(eventListener.captured(e, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListener.captured(e, Event.LIFECYCLE.POST_SAVE));
    }

    @Test
    public void shouldFireEventsWhenUpdatingExistingEntity() {
        eventListener = new TestEventListener();
        session.register(eventListener);

        a.setName("newA");
        session.save(a);

        assertEquals(2, eventListener.count());

        assertTrue(eventListener.captured(a, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListener.captured(a, Event.LIFECYCLE.POST_SAVE));

    }

    @Test
    public void shouldFireEventsWhenSettingNullProperty() {
        eventListener = new TestEventListener();
        session.register(eventListener);

        a.setName(null);
        session.save(a);

        assertEquals(2, eventListener.count());

        assertTrue(eventListener.captured(a, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListener.captured(a, Event.LIFECYCLE.POST_SAVE));

    }

    @Test
    public void shouldFireEventsWhenDeletingRelationshipEntity() {

        eventListener = new TestEventListener();
        session.register(eventListener);

        session.delete(knowsJL);

        assertTrue(eventListener.captured(knowsJL, Event.LIFECYCLE.PRE_DELETE));
        assertTrue(eventListener.captured(knowsJL, Event.LIFECYCLE.POST_DELETE));

        assertTrue(eventListener.captured(jim, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListener.captured(jim, Event.LIFECYCLE.POST_SAVE));

        assertTrue(eventListener.captured(lee, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListener.captured(lee, Event.LIFECYCLE.POST_SAVE));

        assertEquals(6, eventListener.count());

    }

    @Test
    public void shouldNotFireEventsWhenDeletingNonPersistedObject() {

        Document unpersistedDocument = new Document();

        eventListener = new TestEventListener();
        session.register(eventListener);

        session.delete(unpersistedDocument);

        assertEquals(0, eventListener.count());

    }


    @Test
    public void shouldFireEventsWhenUpdatingRelationshipEntity() {

        eventListener = new TestEventListener();
        session.register(eventListener);

        Random r = new Random();
        knowsJL.setSince((new Date((long) (1293861599 + r.nextDouble() * 60 * 60 * 24 * 365))));

        session.save(knowsJL);

        assertTrue(eventListener.captured(knowsJL, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListener.captured(knowsJL, Event.LIFECYCLE.POST_SAVE));

        assertEquals(2, eventListener.count());


    }

    @Test
    public void shouldFireEventsWhenCreatingRelationshipEntity() {

        eventListener = new TestEventListener();
        session.register(eventListener);

        Random r = new Random();
        Knows knowsBS = new Knows();

        knowsBS.setFirstActor(bruce);
        knowsBS.setSecondActor(stan);
        knowsBS.setSince((new Date((long) (1293861599 + r.nextDouble() * 60 * 60 * 24 * 365))));

        bruce.getKnows().add(knowsBS);

        session.save(bruce);

        assertTrue(eventListener.captured(knowsBS, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListener.captured(knowsBS, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListener.captured(bruce, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListener.captured(bruce, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListener.captured(stan, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListener.captured(stan, Event.LIFECYCLE.POST_SAVE));

        assertEquals(6, eventListener.count());
    }

}
