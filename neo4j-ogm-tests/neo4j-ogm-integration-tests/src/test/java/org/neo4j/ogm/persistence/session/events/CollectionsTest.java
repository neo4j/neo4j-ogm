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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
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

    @Test // GH-473
    public void shouldFireEventsOnSavingIterables() {

        List<Actor> actors = Arrays.asList(new Actor("Arnold"), new Actor("Bruce"));

        Iterable<Actor> iterableActors = actors.stream()::iterator;
        session.save(iterableActors);

        EnumSet<Event.TYPE> preAndPostSave = EnumSet.of(Event.TYPE.PRE_SAVE, Event.TYPE.POST_SAVE);
        actors.forEach(actor ->
            preAndPostSave.forEach(type -> assertThat(eventListener.captured(actor, type)))
        );
    }

    @Test // GH-473
    public void shouldFireEventsOnDeletingIterables() {

        List<Actor> actors = Arrays.asList(new Actor("Sylvester"), new Actor("Chuck"));
        actors.forEach(session::save);

        Iterable<Actor> iterableActors = actors.stream()::iterator;
        session.delete(iterableActors);

        EnumSet<Event.TYPE> preAndPostDelete = EnumSet.of(Event.TYPE.PRE_DELETE, Event.TYPE.POST_DELETE);
        actors.forEach(actor ->
            preAndPostDelete.forEach(type -> assertThat(eventListener.captured(actor, type)))
        );
    }
}
