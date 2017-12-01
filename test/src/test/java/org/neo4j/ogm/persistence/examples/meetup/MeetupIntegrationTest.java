/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices and license terms. Your use of the source code for these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 *
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
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Luanne Misquitta
 */
public class MeetupIntegrationTest extends MultiDriverTestClass {

    private static Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory("org.neo4j.ogm.domain.meetup").openSession();
    }

    @After
    public void clear() {
        session.purgeDatabase();
    }

    /**
     * @see Issue 276
     */
    @Test
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
