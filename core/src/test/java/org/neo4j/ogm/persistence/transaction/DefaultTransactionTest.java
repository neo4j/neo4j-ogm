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

package org.neo4j.ogm.persistence.transaction;

import java.io.IOException;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.ogm.domain.social.User;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test of behaviour of the Session for default transactions (transactions without explicit handling)
 *
 * @author Frantisek Hartman
 */
public class DefaultTransactionTest extends MultiDriverTestClass {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTransactionTest.class);

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void setUpClass() throws Exception {
        sessionFactory = new SessionFactory("org.neo4j.ogm.domain.social");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        Result result = session.query("CREATE CONSTRAINT ON (u:User) ASSERT u.name IS UNIQUE", Collections.EMPTY_MAP);
    }

    @After
    public void tearDown() throws Exception {
        session.query("DROP CONSTRAINT ON (u:User) ASSERT u.name IS UNIQUE", Collections.EMPTY_MAP);
    }

    @Test
    public void shouldBeAbleToUseSessionAfterDefaultTransactionFails() throws Exception {

        User u1 = new User("frantisek");
        session.save(u1);
        session.clear();

        try {
            session.save(new User("frantisek"));
            fail("Constraint violation should have make the second save fail");
        } catch (Exception ex) {
            logger.info("Caught exception", ex);
        }

        User loaded = session.load(User.class, u1.getId());
        assertNotNull(loaded);
    }
}
