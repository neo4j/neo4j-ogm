/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

package org.neo4j.ogm.persistence.types.properties;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

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
}
