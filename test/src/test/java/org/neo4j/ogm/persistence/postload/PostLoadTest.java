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

package org.neo4j.ogm.persistence.postload;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.postload.User;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * Test for {@link org.neo4j.ogm.annotation.PostLoad} annotation behaviour
 *
 * @author Frantisek Hartman
 */
public class PostLoadTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.postload");
    }

    @Before
    public void setUp() throws Exception {
        session = sessionFactory.openSession();
        session.purgeDatabase();
        User.resetPostLoadCount();
    }

    @Test
    public void shouldCallPostLoadMethod() throws Exception {
        User user = new User();
        session.save(user);

        session.clear();

        User loaded = session.load(User.class, user.getId());
        assertThat(loaded).isNotNull();

        assertThat(User.getPostLoadCount()).isEqualTo(1);
    }

    @Test
    public void shouldCallPostLoadMethodWhenEntityIsInSession() throws Exception {
        User user = new User();
        session.save(user);

        session.load(User.class, user.getId());
        session.load(User.class, user.getId());

        assertThat(User.getPostLoadCount()).isEqualTo(2);
    }

    @Test
    public void shouldCallPostLoadForEachEntityOnce() throws Exception {
        User commonFriend = new User();
        User u1 = new User();
        User u2 = new User();

        commonFriend.addFriend(u1);
        commonFriend.addFriend(u2);

        session.save(commonFriend);
        session.clear();

        // this returns multiple rows (2^3 = 8 rows), but should execute post load only once when all entities are
        // hydrated
        session.query("MATCH (u:User)-[rel]-(friend:User) RETURN u,rel,friend", Collections.emptyMap());

        assertThat(User.getPostLoadCount()).isEqualTo(3);
    }
}
