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
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.event.Event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author vince
 */
public class NodeEntityTest extends EventTest {

    @Test
    public void shouldNotFireEventsIfObjectHasNotChanged() {

        session.save(folder);
        assertEquals(0, eventListener.count());
    }

    @Test
    public void shouldNotFireEventsOnLoad() {

        session.load(Folder.class, folder.getId());
        assertEquals(0, eventListener.count());
    }

    @Test
    public void shouldFireEventsWhenCreatingNewEntity() {

        Document e = new Document();
        e.setName("e");
        session.save(e);

        assertEquals(2, eventListener.count());

        assertTrue(eventListener.captured(e, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(e, Event.TYPE.POST_SAVE));
    }

    @Test
    public void shouldFireEventsWhenUpdatingExistingEntity() {

        a.setName("newA");
        session.save(a);

        assertEquals(2, eventListener.count());

        assertTrue(eventListener.captured(a, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(a, Event.TYPE.POST_SAVE));

    }

    @Test
    public void shouldFireEventsWhenSettingNullProperty() {

        a.setName(null);
        session.save(a);

        assertEquals(2, eventListener.count());

        assertTrue(eventListener.captured(a, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(a, Event.TYPE.POST_SAVE));

    }

    @Test
    public void shouldNotFireEventsWhenDeletingNonPersistedObject() {

        Document unpersistedDocument = new Document();

        session.delete(unpersistedDocument);

        assertEquals(0, eventListener.count());

    }

    @Test
    public void shouldFireEventsWhenDeletingRelationshipEntity() {

        session.delete(knowsJL);

        assertTrue(eventListener.captured(knowsJL, Event.TYPE.PRE_DELETE));
        assertTrue(eventListener.captured(knowsJL, Event.TYPE.POST_DELETE));

        assertTrue(eventListener.captured(jim, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(jim, Event.TYPE.POST_SAVE));

        assertTrue(eventListener.captured(lee, Event.TYPE.PRE_SAVE));
        assertTrue(eventListener.captured(lee, Event.TYPE.POST_SAVE));

        assertEquals(6, eventListener.count());

    }
}
