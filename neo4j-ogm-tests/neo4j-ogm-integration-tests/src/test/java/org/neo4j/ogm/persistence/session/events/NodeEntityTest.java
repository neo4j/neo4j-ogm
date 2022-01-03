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
package org.neo4j.ogm.persistence.session.events;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.PostSaveEvent;
import org.neo4j.ogm.session.event.PreSaveEvent;

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

        Event captured = eventListener.get(e, Event.TYPE.PRE_SAVE);
        assertThat(captured).isNotNull();
        assertThat(((PreSaveEvent) captured).isNew()).isTrue();
        captured = eventListener.get(e, Event.TYPE.POST_SAVE);
        assertThat(captured).isNotNull();
        assertThat(((PostSaveEvent) captured).wasNew()).isTrue();
    }

    @Test
    public void shouldFireEventsWhenUpdatingExistingEntity() {

        a.setName("newA");
        session.save(a);

        assertThat(eventListener.count()).isEqualTo(2);

        Event captured = eventListener.get(a, Event.TYPE.PRE_SAVE);
        assertThat(captured).isNotNull();
        assertThat(((PreSaveEvent) captured).isNew()).isFalse();
        captured = eventListener.get(a, Event.TYPE.POST_SAVE);
        assertThat(captured).isNotNull();
        assertThat(((PostSaveEvent) captured).wasNew()).isFalse();
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
