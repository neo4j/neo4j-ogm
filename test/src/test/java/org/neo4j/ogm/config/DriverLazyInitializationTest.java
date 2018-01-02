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

package org.neo4j.ogm.config;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.domain.simple.User;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestServer;

/**
 * Tests for lazy initialization of Bolt and Http drivers
 * Not using {@link org.neo4j.ogm.testutil.MultiDriverTestClass} test class because we actually test behaviour when
 * server is down at SessionFactory creation.
 *
 * @author Frantisek Hartman
 */
@Ignore // ignored because of the test runner on team city, child classes should run normally
public abstract class DriverLazyInitializationTest {

    protected Configuration.Builder configBuilder;

    @Test
    public void shouldCreateSessionFactoryWhenServerIsOffline() throws Exception {
        Configuration configuration = configBuilder.build();

        SessionFactory sessionFactory = new SessionFactory(configuration, User.class.getPackage().getName());
        assertThat(sessionFactory.getDriver()).isNotNull();
    }

    @Test(expected = ConnectionException.class)
    public void shouldThrowServiceUnavailableWhenServerIsOfflineAndVerifyIsTrue() throws Exception {
        Configuration configuration = configBuilder.verifyConnection(true)
            .build();

        new SessionFactory(configuration, User.class.getPackage().getName());
    }

    @Test
    public void shouldInitialiseDriverAfterServerComesOnline() throws Exception {
        TestServer testServer = new TestServer(true, true, 5);
        String uri = testServer.getUri();
        testServer.shutdown();

        Configuration configuration = configBuilder
            .uri(uri)
            .build();

        SessionFactory sessionFactory = new SessionFactory(configuration, User.class.getPackage().getName());

        testServer = new TestServer(false, true, 5, testServer.getPort());
        Session session = sessionFactory.openSession();
        User user = new User("John Doe");
        session.save(user);

        User loaded = session.load(User.class, user.getId());
        assertThat(loaded.getName()).isEqualTo("John Doe");
    }
}
