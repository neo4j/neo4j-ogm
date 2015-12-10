/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.integration.cineasts.partial;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.partial.Actor;
import org.neo4j.ogm.domain.cineasts.partial.Movie;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.IntegrationTestRule;

/**
 * The purpose of these tests is to describe the behaviour of the
 * mapper when a RelationshipEntity object is not referenced by
 * both of its Related entities, both when writing and reading
 *
 * In this scenario, the Role relationship, which is a RelationshipEntity
 * linking Actors and Movies, is referenced only from the Actor entity.
 *
 * @author Vince Bickers
 */
public class RelationshipEntityPartialMappingTest {

    @Rule
    public final IntegrationTestRule testServer = new IntegrationTestRule(Components.driver());

    private Session session;

    @Before
    public void init() {
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.partial");
        session = sessionFactory.openSession(testServer.driver());
        session.purgeDatabase();
    }

    @Test
    public void testCreateAndReloadActorRoleAndMovie() {

        Actor keanu = new Actor("Keanu Reeves");
        Movie matrix = new Movie("The Matrix");
        keanu.addRole("Neo", matrix);

        session.save(keanu);

        Actor keanu2 = session.load(Actor.class, keanu.getId());

        assertEquals(1, keanu2.roles().size());

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

        assertEquals(2, keanu2.roles().size());

        keanu2.addRole("John Constantine", new Movie("Constantine"));
        session.save(keanu2);

        Actor keanu3 = session.load(Actor.class, keanu2.getId());
        assertEquals(3, keanu3.roles().size());

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

        assertEquals(2, keanu2.roles().size());

        keanu2.removeRole("Dumbledore");

        session.save(keanu2);

        Actor keanu3 = session.load(Actor.class, keanu2.getId());
        assertEquals(1, keanu3.roles().size());


    }

}
