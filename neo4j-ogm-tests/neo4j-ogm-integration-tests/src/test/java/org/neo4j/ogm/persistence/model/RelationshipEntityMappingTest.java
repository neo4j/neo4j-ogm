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

import static org.junit.Assert.*;

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
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
public class RelationshipEntityMappingTest extends MultiDriverTestClass {

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.cineasts.annotated",
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
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "MERGE (m:Movie {uuid:\"" + hp.getUuid().toString()
            + "\"}) SET m.title = 'Goblet of Fire', m.year = 2005 MERGE (a:Actor {uuid:\"" + daniel.getUuid().toString()
            + "\"}) SET a.name='Daniel Radcliffe' create (a)-[:ACTS_IN {role:'Harry Potter'}]->(m)");
    }

    @Test
    public void testThatRelationshipEntityNameIsUsedAsRelationshipTypeWhenTypeIsNotDefined() {
        Movie hp = new Movie("Goblet of Fire", 2005);

        Actor daniel = new Actor("Daniel Radcliffe");
        daniel.nominatedFor(hp, "Saturn Award", 2005);
        session.save(daniel);
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "MERGE (m:Movie {uuid:\"" + hp.getUuid().toString()
            + "\"}) SET m.title = 'Goblet of Fire', m.year = 2005 MERGE (a:Actor {uuid:\"" + daniel.getUuid().toString()
            + "\"}) SET a.name='Daniel Radcliffe' create (a)-[:NOMINATIONS {name:'Saturn Award', year:2005}]->(m)");
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
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (a:A) " +
                "CREATE (b:B) " +
                "CREATE (a)-[:CR]->(b)");
    }

    @Test
    public void shouldBeAbleToSaveAndLoadRelationshipEntityWithNullProperties() throws Exception {
        Actor keanu = new Actor("Keanu Reeves");

        Movie matrix = new Movie("The Matrix", 1999);
        HashSet<Role> roles = new HashSet<>();
        Role role = new Role(matrix, keanu, null);
        roles.add(role);
        keanu.setRoles(roles);

        session.save(keanu);

        Map<String, Object> params = new HashMap<>();
        params.put("actorId", keanu.getId());
        Result result = session.query("MATCH (a:Actor)-[r:ACTS_IN]-(m:Movie) WHERE ID(a) = {actorId} RETURN r as rel",
            params);

        Iterator<Map<String, Object>> iterator = result.iterator();

        Map<String, Object> first = iterator.next();
        assertSame(role, first.get("rel"));
    }
}
