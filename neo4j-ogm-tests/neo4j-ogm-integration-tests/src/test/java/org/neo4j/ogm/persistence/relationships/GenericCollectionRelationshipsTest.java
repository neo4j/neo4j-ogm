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
package org.neo4j.ogm.persistence.relationships;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.gh385.RichRelations.E;
import org.neo4j.ogm.domain.gh385.RichRelations.R;
import org.neo4j.ogm.domain.gh385.RichRelations.S;
import org.neo4j.ogm.domain.gh385.SimpleRelations;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Michael J. Simons
 */
public class GenericCollectionRelationshipsTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    @BeforeClass
    public static void initSesssionFactory() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.gh385");
    }

    @Test // GH-385
    public void shouldLoadSimpleRelationshipIntoCollection() {
        long id = (long) sessionFactory.openSession().query(""
            + "CREATE (p:P {name: 'P'})\n"
            + "MERGE (p) - [:HAS] -> (:C {name: 'C1'})\n"
            + "MERGE (p) - [:HAS] -> (:C {name: 'C2'})\n"
            + "RETURN id(p) AS id", emptyMap()
        ).queryResults().iterator().next().get("id");

        Session session = sessionFactory.openSession();
        SimpleRelations.P p = session.load(SimpleRelations.P.class, id);

        assertThat(p)
            .isNotNull()
            .extracting(SimpleRelations.P::getC).asList()
            .hasSize(2);
    }

    @Test // GH-385
    public void shouldLoadRichRelationshipIntoCollection() {
        long id = (long) sessionFactory.openSession().query(""
            + "CREATE (s:S)\n"
            + "MERGE (s) - [:R {name: 'Same'}] -> (:E {name: 'E1'})\n"
            + "MERGE (s) - [:R {name: 'Same'}] -> (:E {name: 'E2'})\n"
            + "RETURN id(s) AS id", emptyMap()
        ).queryResults().iterator().next().get("id");

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
