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

package org.neo4j.ogm.persistence.session.events;

import static org.assertj.core.api.Assertions.*;

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

        assertThat(eventListener.count()).isEqualTo(6);

        assertThat(eventListener.captured(a, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(a, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(eventListener.captured(b, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(b, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(eventListener.captured(c, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(c, Event.TYPE.POST_SAVE)).isTrue();
    }

    @Test
    public void shouldFireEventsWhenDeletingACollection() {

        session.delete(Arrays.asList(a, b, c));

        assertThat(eventListener.captured(a, Event.TYPE.PRE_DELETE)).isTrue();
        assertThat(eventListener.captured(a, Event.TYPE.POST_DELETE)).isTrue();
        assertThat(eventListener.captured(b, Event.TYPE.PRE_DELETE)).isTrue();
        assertThat(eventListener.captured(b, Event.TYPE.POST_DELETE)).isTrue();
        assertThat(eventListener.captured(c, Event.TYPE.PRE_DELETE)).isTrue();
        assertThat(eventListener.captured(c, Event.TYPE.POST_DELETE)).isTrue();

        // even though we haven't updated the folder object, the database
        // has removed the relationships between the folder and the documents, so
        // the folder events must fire
        assertThat(eventListener.captured(folder, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(8);
    }

    @Test
    public void shouldFireEventWhenDeletingAllObjectsOfASpecifiedType() {

        session.deleteAll(Document.class);

        assertThat(eventListener.count()).isEqualTo(2);

        assertThat(eventListener.captured(Document.class, Event.TYPE.PRE_DELETE)).isTrue();
        assertThat(eventListener.captured(Document.class, Event.TYPE.POST_DELETE)).isTrue();
    }

    @Test
    public void shouldFireEventsWhenDeletingObjectsOfDifferentTypes() {

        session.delete(Arrays.asList(folder, knowsJB));

        // object deletes
        assertThat(eventListener.captured(folder, Event.TYPE.PRE_DELETE)).isTrue();
        assertThat(eventListener.captured(folder, Event.TYPE.POST_DELETE)).isTrue();
        assertThat(eventListener.captured(knowsJB, Event.TYPE.PRE_DELETE)).isTrue();
        assertThat(eventListener.captured(knowsJB, Event.TYPE.POST_DELETE)).isTrue();

        // document updates
        assertThat(eventListener.captured(a, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(a, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(eventListener.captured(b, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(b, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(eventListener.captured(c, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(c, Event.TYPE.POST_SAVE)).isTrue();

        // people updates
        assertThat(eventListener.captured(jim, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(jim, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(eventListener.captured(bruce, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(bruce, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(14);
    }
}
