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
