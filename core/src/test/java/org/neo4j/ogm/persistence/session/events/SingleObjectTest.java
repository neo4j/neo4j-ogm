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

        this.eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        session.save(folder);
        assertEquals(0, eventListenerTest.count());
    }

    @Test
    public void shouldNotFireEventsOnLoad() {

        this.eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        session.load(Folder.class, folder.getId());
        assertEquals(0, eventListenerTest.count());
    }

    @Test
    public void shouldFireEventsWhenCreatingNewEntity() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        Document e = new Document();
        e.setName("e");
        session.save(e);

        assertEquals(2, eventListenerTest.count());

        assertTrue(eventListenerTest.captured(e, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(e, Event.LIFECYCLE.POST_SAVE));
    }

    @Test
    public void shouldFireEventsWhenUpdatingExistingEntity() {
        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        a.setName("newA");
        session.save(a);

        assertEquals(2, eventListenerTest.count());

        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.POST_SAVE));

    }

    @Test
    public void shouldFireEventsWhenSettingNullProperty() {
        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        a.setName(null);
        session.save(a);

        assertEquals(2, eventListenerTest.count());

        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(a, Event.LIFECYCLE.POST_SAVE));

    }

    @Test
    public void shouldFireEventsWhenDeletingRelationshipEntity() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        session.delete(knowsJL);

        assertEquals(2, eventListenerTest.count());

        assertTrue(eventListenerTest.captured(knowsJL, Event.LIFECYCLE.PRE_DELETE));
        assertTrue(eventListenerTest.captured(knowsJL, Event.LIFECYCLE.POST_DELETE));

    }

    @Test
    public void shouldNotFireEventsWhenDeletingNonPersistedObject() {

        Document unpersistedDocument = new Document();

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        session.delete(unpersistedDocument);

        assertEquals(0, eventListenerTest.count());

    }


    @Test
    public void shouldFireEventsWhenUpdatingRelationshipEntity() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        Random r = new Random();
        knowsJL.setSince((new Date((long) (1293861599 + r.nextDouble() * 60 * 60 * 24 * 365))));

        session.save(knowsJL);

        assertTrue(eventListenerTest.captured(knowsJL, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(knowsJL, Event.LIFECYCLE.POST_SAVE));

        assertEquals(2, eventListenerTest.count());


    }

    @Test
    public void shouldFireEventsWhenCreatingRelationshipEntity() {

        eventListenerTest = new EventListenerTest();
        session.register(eventListenerTest);

        Random r = new Random();
        Knows knowsBS = new Knows();

        knowsBS.setFirstActor(bruce);
        knowsBS.setSecondActor(stan);
        knowsBS.setSince((new Date((long) (1293861599 + r.nextDouble() * 60 * 60 * 24 * 365))));

        bruce.getKnows().add(knowsBS);

        session.save(bruce);

        assertTrue(eventListenerTest.captured(knowsBS, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(knowsBS, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListenerTest.captured(bruce, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(bruce, Event.LIFECYCLE.POST_SAVE));
        assertTrue(eventListenerTest.captured(stan, Event.LIFECYCLE.PRE_SAVE));
        assertTrue(eventListenerTest.captured(stan, Event.LIFECYCLE.POST_SAVE));

        assertEquals(6, eventListenerTest.count());
    }

}
