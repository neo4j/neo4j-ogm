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
package org.neo4j.ogm.persistence.examples.cineasts.partial;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.minimum.Actor;
import org.neo4j.ogm.domain.cineasts.minimum.Movie;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * The purpose of these tests is to describe the behaviour of the
 * mapper when a RelationshipEntity object is not referenced by
 * both of its Related entities, both when writing and reading
 * In this scenario, the Role relationship, which is a RelationshipEntity
 * linking Actors and Movies, is referenced only from the Actor entity.
 *
 * @author Vince Bickers
 */
public class RelationshipEntityPartialMappingTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.cineasts.minimum");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void testCreateAndReloadActorRoleAndMovie() {

        Actor keanu = new Actor("Keanu Reeves");
        Movie matrix = new Movie("The Matrix");
        keanu.addRole("Neo", matrix);

        session.save(keanu);

        Actor keanu2 = session.load(Actor.class, keanu.getId());

        assertThat(keanu2.roles()).hasSize(1);
    }

    @Test
    public void testCreateAndReloadActorMultipleRolesAndMovies() {

        Actor keanu = new Actor("Keanu Reeves");
        Movie matrix = new Movie("The Matrix");
        Movie speed = new Movie("Speed");

        keanu.addRole("Neo", matrix);
        keanu.addRole("Jack Traven", speed);

        session.save(keanu);

        Actor keanu2 = session.load(Actor.class, keanu.getId());

        assertThat(keanu2.roles()).hasSize(2);

        keanu2.addRole("John Constantine", new Movie("Constantine"));
        session.save(keanu2);

        Actor keanu3 = session.load(Actor.class, keanu2.getId());
        assertThat(keanu3.roles()).hasSize(3);
    }

    @Test
    public void testCreateAndDeleteActorMultipleRolesAndMovies() {

        Actor keanu = new Actor("Keanu Reeves");
        Movie matrix = new Movie("The Matrix");
        Movie hp = new Movie("Harry Potter");

        keanu.addRole("Neo", matrix);
        keanu.addRole("Dumbledore", hp);

        session.save(keanu);

        Actor keanu2 = session.load(Actor.class, keanu.getId());

        assertThat(keanu2.roles()).hasSize(2);

        keanu2.removeRole("Dumbledore");

        session.save(keanu2);

        Actor keanu3 = session.load(Actor.class, keanu2.getId());
        assertThat(keanu3.roles()).hasSize(1);
    }
}
