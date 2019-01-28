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
package org.neo4j.ogm.persistence.relationships;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.domain.gh385.RichRelations.E;
import org.neo4j.ogm.domain.gh385.RichRelations.R;
import org.neo4j.ogm.domain.gh385.RichRelations.S;
import org.neo4j.ogm.domain.gh385.SimpleRelations.P;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Michael J. Simons
 */
public class GenericCollectionRelationshipsTest extends MultiDriverTestClass {

    @BeforeClass
    public static void initSesssionFactory() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.gh385");
    }

    @Test // GH-385
    public void shouldLoadSimpleRelationshipIntoCollection() {

        final GraphDatabaseService database = getGraphDatabaseService();
        long id = (long) database.execute(""
            + "CREATE (p:P {name: 'P'})\n"
            + "MERGE (p) - [:HAS] -> (:C {name: 'C1'})\n"
            + "MERGE (p) - [:HAS] -> (:C {name: 'C2'})\n"
            + "RETURN id(p) AS id"
        ).next().get("id");

        Session session = sessionFactory.openSession();
        P p = session.load(P.class, id);

        assertThat(p)
            .isNotNull()
            .extracting(P::getC).asList()
            .hasSize(2);
    }

    @Test // GH-385
    public void shouldLoadRichRelationshipIntoCollection() {

        final GraphDatabaseService database = getGraphDatabaseService();
        long id = (long) database.execute(""
            + "CREATE (s:S)\n"
            + "MERGE (s) - [:R {name: 'Same'}] -> (:E {name: 'E1'})\n"
            + "MERGE (s) - [:R {name: 'Same'}] -> (:E {name: 'E2'})\n"
            + "RETURN id(s) AS id"
        ).next().get("id");

        Session session = sessionFactory.openSession();
        S s = session.load(S.class, id);

        assertThat(s)
            .isNotNull();

        assertThat(s.getR())
            .extracting(R::getName)
            .containsExactly("Same", "Same");

        assertThat(s.getR())
            .extracting(R::getE)
            .extracting(E::getName)
            .containsExactlyInAnyOrder("E1", "E2");
    }

    @Test // GH-385
    public void shouldWriteRichRelationshipFromCollection() {
        Session session = sessionFactory.openSession();

        S s = new S("Test");
        E e1 = new E("Ende1");
        E e2 = new E("Ende2");

        s.setR(Arrays.asList(new R(s, e1, "SomeName"), new R(s, e2, "SomeName")));
        session.save(s);
        session.clear();

        S reloaded = session.load(S.class, s.getId());

        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getR())
            .extracting(R::getE)
            .extracting(E::getName)
            .containsExactlyInAnyOrder("Ende1", "Ende2");
    }
}
