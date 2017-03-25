/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.domain.knowledge;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author vince
 */
public class KnowledgeTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(baseConfiguration.build(), "org.neo4j.ogm.domain.knowledge");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
    }

    /**
     * @see 351
     */
    @Test
    public void shouldBeAbleToRollbackObjectWithDifferentKnowledges() {

        Person john = new Person("John");
        Person mary = new Person("Mary");

        Language java = new Language("Java");
        Language scala = new Language("Scala");

        john.knows(mary);
        john.knows(java);
        john.knows(scala);

        try (Transaction tx = session.beginTransaction()) {

            session.save(john);

            tx.rollback();
        }
    }
}
