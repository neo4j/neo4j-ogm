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

import java.util.Date;
import java.util.Random;

import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.annotated.Knows;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.PostSaveEvent;
import org.neo4j.ogm.session.event.PreSaveEvent;

/**
 * @author vince
 */
public class RelationshipEntityTest extends EventTestBaseClass {

    @Test
    public void shouldNotFireEventsIfObjectHasNotChanged() {

        session.save(folder);
        assertThat(eventListener.count()).isEqualTo(0);
    }

    @Test
    public void shouldFireEventsWhenUpdatingRelationshipEntity() {

        Random r = new Random();
        knowsJL.setSince((new Date((long) (1293861599 + r.nextDouble() * 60 * 60 * 24 * 365))));

        session.save(knowsJL);

        assertThat(eventListener.captured(knowsJL, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(eventListener.captured(knowsJL, Event.TYPE.POST_SAVE)).isTrue();

        assertThat(eventListener.count()).isEqualTo(2);
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

    @Test
    public void shouldFireEventsWhenCreatingRelationshipEntity() {

        Knows knowsBS = new Knows();

        knowsBS.setFirstActor(bruce);
        knowsBS.setSecondActor(stan);

        bruce.getKnows().add(knowsBS);

        session.save(bruce);

        assertThat(eventListener.captured(knowsBS, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(((PreSaveEvent) eventListener.get(knowsBS, Event.TYPE.PRE_SAVE)).isNew()).isTrue();
        assertThat(eventListener.captured(knowsBS, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(((PostSaveEvent) eventListener.get(knowsBS, Event.TYPE.POST_SAVE)).wasNew()).isTrue();
        assertThat(eventListener.captured(bruce, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(((PreSaveEvent) eventListener.get(bruce, Event.TYPE.PRE_SAVE)).isNew()).isFalse();
        assertThat(eventListener.captured(bruce, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(((PostSaveEvent) eventListener.get(bruce, Event.TYPE.POST_SAVE)).wasNew()).isFalse();
        assertThat(eventListener.captured(stan, Event.TYPE.PRE_SAVE)).isTrue();
        assertThat(((PreSaveEvent) eventListener.get(stan, Event.TYPE.PRE_SAVE)).isNew()).isFalse();
        assertThat(eventListener.captured(stan, Event.TYPE.POST_SAVE)).isTrue();
        assertThat(((PostSaveEvent) eventListener.get(stan, Event.TYPE.POST_SAVE)).wasNew()).isFalse();

        assertThat(eventListener.count()).isEqualTo(6);
    }
}
