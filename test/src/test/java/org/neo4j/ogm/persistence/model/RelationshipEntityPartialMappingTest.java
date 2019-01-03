/*
 * Copyright (c) 2002-2019 "Neo4j,"
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

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.minimum.Actor;
import org.neo4j.ogm.domain.cineasts.minimum.Movie;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * The purpose of these tests is to describe the behaviour of the
 * mapper when a RelationshipEntity object is not referenced by
 * both of its Related entities.
 *
 * @author Vince Bickers
 */
public class RelationshipEntityPartialMappingTest extends MultiDriverTestClass {

    private Session session;

    @BeforeClass
    public static void oneTimeSetup() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.cineasts.minimum");
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
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "create (a:Actor {name:'Keanu Reeves'}) " +
                "create (m:Movie {name:'The Matrix'}) " +
                "create (a)-[:ACTS_IN {played:'Neo'}]->(m)");
    }
}
