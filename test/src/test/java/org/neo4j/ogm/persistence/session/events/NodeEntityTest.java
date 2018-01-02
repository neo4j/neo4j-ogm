/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.event.Event;

/**
 * @author vince
 */
public class NodeEntityTest extends EventTestBaseClass {

    @Test
    public void shouldNotFireEventsIfObjectHasNotChanged() {

        session.save(folder);
        assertThat(eventListener.count()).isEqualTo(0);
    }

    @Test
    public void shouldNotFireEventsOnLoad() {

        session.load(Folder.class, folder.getId());
        assertThat(eventListener.count()).isEqualTo(0);
    }

    @Test
    public void shouldFireEventsWhenCreatingNewEntity() {

        Document e = new Document();
        e.setName("e");
        session.save(e);

        assertThat(eventListener.count()).isEqualTo(2);

        assertThat(eventListener.captured(e, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(e, Event.TYPE.POST_SAVE)).isTrue();
    }

    @Test
    public void shouldFireEventsWhenUpdatingExistingEntity() {

        a.setName("newA");
        session.save(a);

        assertThat(eventListener.count()).isEqualTo(2);

        assertThat(eventListener.captured(a, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(a, Event.TYPE.POST_SAVE)).isTrue();
    }

    @Test
    public void shouldFireEventsWhenSettingNullProperty() {

        a.setName(null);
        session.save(a);

        assertThat(eventListener.count()).isEqualTo(2);

        assertThat(eventListener.captured(a, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(a, Event.TYPE.POST_SAVE)).isTrue();
    }

    @Test
    public void shouldNotFireEventsWhenDeletingNonPersistedObject() {

        Document unpersistedDocument = new Document();

        session.delete(unpersistedDocument);

        assertThat(eventListener.count()).isEqualTo(0);
    }

    @Test
    public void shouldFireEventsWhenDeletingRelationshipEntity() {

        session.delete(knowsJL);

        assertThat(eventListener.captured(knowsJL, Event.TYPE.PRE_DELETE)).isTrue();
        assertThat(eventListener.captured(knowsJL, Event.TYPE.POST_DELETE)).isTrue();

        assertThat(eventListener.captured(jim, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(jim, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.captured(lee, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(lee, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(6);
    }
}
