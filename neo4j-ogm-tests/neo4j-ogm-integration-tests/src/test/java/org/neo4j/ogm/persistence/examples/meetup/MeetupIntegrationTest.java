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
package org.neo4j.ogm.persistence.examples.meetup;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.meetup.Meetup;
import org.neo4j.ogm.domain.meetup.Person;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 * @author Gerrit Meier
 */
public class MeetupIntegrationTest extends TestContainersTestBase {

    private static Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.meetup").openSession();
    }

    @After
    public void clear() {
        session.purgeDatabase();
    }

    @Test // GH-276
    public void shouldLoadRelatedPersonsCorrectly() {
        Meetup meetup = new Meetup("Neo4j UAE");
        Person michal = new Person("Michal");
        Person luanne = new Person("Luanne");

        meetup.setOrganiser(michal);
        michal.getMeetupOrganised().add(meetup);
        meetup.getAttendees().add(luanne);
        luanne.getMeetupsAttended().add(meetup);
        session.save(meetup);

        session.clear();

        Meetup neoUae = session.load(Meetup.class, meetup.getId());
        assertThat(neoUae).isNotNull();
        assertThat(neoUae.getOrganiser()).isNotNull();
        assertThat(neoUae.getAttendees()).hasSize(1);

    }
}
