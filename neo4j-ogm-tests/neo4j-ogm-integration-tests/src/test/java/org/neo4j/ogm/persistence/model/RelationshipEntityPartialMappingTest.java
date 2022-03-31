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
package org.neo4j.ogm.persistence.model;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.minimum.Actor;
import org.neo4j.ogm.domain.cineasts.minimum.Movie;
import org.neo4j.ogm.domain.cineasts.minimum.Role;
import org.neo4j.ogm.response.model.RelationshipModel;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * The purpose of these tests is to describe the behaviour of the
 * mapper when a RelationshipEntity object is not referenced by
 * both of its Related entities.
 *
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class RelationshipEntityPartialMappingTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetup() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.cineasts.minimum");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void testCreateActorRoleAndMovie() {

        Actor keanu = new Actor("Keanu Reeves");
        Movie matrix = new Movie("The Matrix");

        // note: this does not establish a role relationship on the matrix
        keanu.addRole("Neo", matrix);

        session.save(keanu);

        session.clear();
        assertThat(session.query("MATCH (a:Actor {name:'Keanu Reeves'}) -[:ACTS_IN {played:'Neo'}]-> (m:Movie {name:'The Matrix'}) " +
                "RETURN a, m", emptyMap()).queryResults()).hasSize(1);
    }

    @Test // GH-727
    public void shouldNotDropUnmappedRelationshipModels() {
        Session session = sessionFactory.openSession();
        Actor actor = new Actor("A1");
        Movie movie = new Movie("M1");
        Role role = new Role("R1", actor, movie);
        session.save(role);

        session = sessionFactory.openSession();
        Iterable<Map<String, Object>> results = session
            .query("MATCH (m) - [r] - (a) WHERE id(a) = $id RETURN r",
                Collections.singletonMap("id", actor.getId())).queryResults();

        assertThat(results).hasSize(1);
        Map<String, Object> row = results.iterator().next();
        assertThat(row).containsKeys("r");
        assertThat(row.get("r")).isNotNull().isInstanceOf(RelationshipModel.class);
    }

    @Test // GH-727
    public void shouldMapSingleRelationshipModel() {
        Session session = sessionFactory.openSession();
        Actor actor = new Actor("A1");
        Movie movie = new Movie("M1");
        Role role = new Role("R1", actor, movie);
        session.save(role);

        session = sessionFactory.openSession();
        Iterable<Map<String, Object>> results = session
            .query("MATCH (m) - [r] - (a) WHERE id(a) = $id RETURN m, r, a",
                Collections.singletonMap("id", actor.getId())).queryResults();

        assertThat(results).hasSize(1);
        Map<String, Object> row = results.iterator().next();
        assertThat(row).containsKeys("r");
        assertThat(row.get("r")).isNotNull().isInstanceOf(Role.class);
    }
}
