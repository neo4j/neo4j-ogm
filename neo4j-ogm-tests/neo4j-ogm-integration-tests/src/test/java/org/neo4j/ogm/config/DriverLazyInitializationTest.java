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
package org.neo4j.ogm.config;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.domain.simple.User;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestServer;

/**
 * Tests for lazy initialization of Bolt and Http drivers
 * Not using {@link org.neo4j.ogm.testutil.TestContainersTestBase} test class because we actually test behaviour when
 * server is down at SessionFactory creation.
 *
 * @author Frantisek Hartman
 */
@Ignore // ignored because of the test runner on team city, child classes should run normally
public abstract class DriverLazyInitializationTest {

    protected Configuration.Builder configBuilder;

    @Test
    public void shouldCreateSessionFactoryWhenServerIsOffline() {
        Configuration configuration = configBuilder.build();

        SessionFactory sessionFactory = new SessionFactory(configuration, User.class.getPackage().getName());
        assertThat(sessionFactory.unwrap(Driver.class)).isNotNull();
    }

    @Test(expected = ConnectionException.class)
    public void shouldThrowServiceUnavailableWhenServerIsOfflineAndVerifyIsTrue() {
        Configuration configuration = configBuilder.verifyConnection(true)
            .build();

        new SessionFactory(configuration, User.class.getPackage().getName());
    }

    @Test
    public void shouldInitialiseDriverAfterServerComesOnline() {
        TestServer testServer = new TestServer(true, 5);
        String uri = testServer.getUri();
        testServer.shutdown();

        Configuration configuration = configBuilder
            .uri(uri)
            .build();

        SessionFactory sessionFactory = new SessionFactory(configuration, User.class.getPackage().getName());

        testServer = new TestServer(true, 5, testServer.getPort());
        Session session = sessionFactory.openSession();
        User user = new User("John Doe");
        session.save(user);

        User loaded = session.load(User.class, user.getId());
        assertThat(loaded.getName()).isEqualTo("John Doe");
    }
}
