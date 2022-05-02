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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.canonical.hierarchies.A;
import org.neo4j.ogm.domain.canonical.hierarchies.B;
import org.neo4j.ogm.domain.canonical.hierarchies.CR;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.domain.cineasts.annotated.Role;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class RelationshipEntityMappingTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.cineasts.annotated",
            "org.neo4j.ogm.domain.canonical.hierarchies");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void testThatAnnotatedRelationshipOnRelationshipEntityCreatesTheCorrectRelationshipTypeInTheGraph() {
        Movie hp = new Movie("Goblet of Fire", 2005);

        Actor daniel = new Actor("Daniel Radcliffe");
        daniel.playedIn(hp, "Harry Potter");
        session.save(daniel);

        session.clear();
        assertThat(session.query("MATCH (m:Movie {uuid:\"" + hp.getUuid().toString() + "\"}) <-[:ACTS_IN {role:'Harry Potter'}]- "
                + "(a:Actor {uuid:\"" + daniel.getUuid().toString() + "\"}) "
                + " WHERE m.title = 'Goblet of Fire' and m.year = 2005 and a.name='Daniel Radcliffe' "
                + " return m, a",
            emptyMap()).queryResults()).hasSize(1);
    }

    @Test
    public void testThatRelationshipEntityNameIsUsedAsRelationshipTypeWhenTypeIsNotDefined() {
        Movie hp = new Movie("Goblet of Fire", 2005);

        Actor daniel = new Actor("Daniel Radcliffe");
        daniel.nominatedFor(hp, "Saturn Award", 2005);
        session.save(daniel);

        session.clear();
        assertThat(session.query("MATCH (m:Movie {uuid:\"" + hp.getUuid().toString() + "\"}) <-[:NOMINATIONS {name:'Saturn Award', year:2005}]- "
                + "(a:Actor {uuid:\"" + daniel.getUuid().toString() + "\"}) "
                + " WHERE m.title = 'Goblet of Fire' and m.year = 2005 and a.name='Daniel Radcliffe'"
                + " RETURN m, a",
            emptyMap()).queryResults()).hasSize(1);
    }

    @Test
    public void shouldUseCorrectTypeFromHierarchyOfRelationshipEntities() {

        A a = new A();
        B b = new B();

        CR r = new CR();
        r.setA(a);
        r.setB(b);

        a.setR(r);

        session.save(a);

        session.clear();
        assertThat(session.query("MATCH (a:A)-[:CR]->(b:B) RETURN a, b", emptyMap()).queryResults()).hasSize(1);
    }

    @Test
    public void shouldBeAbleToSaveAndLoadRelationshipEntityWithNullProperties() {
        Actor keanu = new Actor("Keanu Reeves");

        Movie matrix = new Movie("The Matrix", 1999);
        HashSet<Role> roles = new HashSet<>();
        Role role = new Role(matrix, keanu, null);
        roles.add(role);
        keanu.setRoles(roles);

        session.save(keanu);

        Map<String, Object> params = new HashMap<>();
        params.put("actorId", keanu.getUuid());
        Result result = session.query("MATCH (a:Actor)-[r:ACTS_IN]-(m:Movie) WHERE a.uuid = $actorId RETURN r as rel",
            params, true);

        Iterator<Map<String, Object>> iterator = result.iterator();

        Map<String, Object> first = iterator.next();
        assertThat(role).isSameAs(first.get("rel"));
    }
}
