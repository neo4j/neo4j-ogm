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

package org.neo4j.ogm.persistence.types.properties;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.properties.Place;
import org.neo4j.ogm.domain.properties.User;
import org.neo4j.ogm.domain.properties.Visit;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * Test for @{@link org.neo4j.ogm.annotation.Properties} annotation on @RelationshipEntity class
 *
 * @author Frantisek Hartman
 */
public class RelationshipEntityPropertiesTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void init() throws IOException {
        // Listing concrete classes because package also contains invalid mapping (for testing)
        String[] classes = new String[] { User.class.getName(), Visit.class.getName(), Place.class.getName() };
        sessionFactory = new SessionFactory(driver, classes);
    }

    @Before
    public void setUp() throws Exception {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void shouldSaveAndLoadProperties() throws Exception {
        User user = new User();
        Place place = new Place();
        Visit visit = new Visit(user, place);
        user.addVisit(visit);
        visit.putProperty("note", "some random note about a visit to a place");
        session.save(user);

        session.clear();

        Visit loaded = session.load(Visit.class, visit.getId());
        assertThat(loaded.getProperties()).containsEntry("note", "some random note about a visit to a place");
    }

    @Test // GH-518
    public void shouldBeAbleToDeletePropertiesOnRelationshipsAgain() {
        User user = new User();
        user.setName("James Bond");

        Visit visit = new Visit();

        Map<String, String> initialProperties = new HashMap<>();
        initialProperties.put("a", "007");
        initialProperties.put("b", "4711");

        visit.setUser(user);
        visit.setProperties(initialProperties);
        visit.setPlace(new Place());

        user.setVisits(Collections.singleton(visit));

        session.save(user);
        session.clear();

        User loadedUser = session.load(User.class, user.getId());
        assertThat(loadedUser.getVisits()).hasSize(1);

        Visit loadedVisit = loadedUser.getVisits().stream().findFirst().get();
        assertThat(loadedVisit.getProperties()).containsOnly(entry("a", "007"), entry("b", "4711"));

        loadedVisit.getProperties().remove("b");

        session.save(loadedUser);
        session.clear();

        loadedUser = session.load(User.class, user.getId());
        loadedVisit = loadedUser.getVisits().stream().findFirst().get();
        assertThat(loadedVisit.getProperties()).containsExactly(entry("a", "007"));
    }
}
