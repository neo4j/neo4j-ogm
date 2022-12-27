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
package org.neo4j.ogm.session;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.domain.blog.Author;
import org.neo4j.ogm.domain.pizza.Pizza;
import org.neo4j.ogm.driver.TypeSystem;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Michael J. Simons
 * @author Gerrit Meier
 */
public class SessionFactoryTest extends TestContainersTestBase {

    @Test
    void shouldMergeBasePackages() {

        Configuration configuration = new Configuration.Builder()
            .withBasePackages(Bike.class.getPackage().getName())
            .uri("bolt://somewhere") // some bolt url to avoid embedded driver (and database) creation
            .build();

        SessionFactory sessionFactory = new SessionFactory(configuration, Author.class.getPackage().getName());
        assertThat(sessionFactory.metaData().classInfo(Bike.class)).isNotNull();
        assertThat(sessionFactory.metaData().classInfo(Author.class)).isNotNull();
        assertThat(sessionFactory.metaData().classInfo(Pizza.class)).isNull();
    }

    @Test
    void shouldUnwrapBoltDriver() {

        SessionFactory sessionFactory = new SessionFactory(getDriver(), Bike.class.getPackage().getName());

        // Neo4j-OGM Driver
        assertThat(sessionFactory.unwrap(BoltDriver.class))
            .isInstanceOf(BoltDriver.class);
        // Native driver
        assertThat(sessionFactory.unwrap(Driver.class))
            .isInstanceOf(Driver.class);

    }

    @Test
    void correctUseStrictQueryingSettingShouldBeApplied() {

        SessionFactory sessionFactory;

        sessionFactory = new SessionFactory(new Configuration.Builder().uri("bolt://something:7687").build(), "org.neo4j.ogm.domain.gh651");
        assertThat(sessionFactory.isUseStrictQuerying()).isTrue();

        sessionFactory = new SessionFactory(new Configuration.Builder().uri("bolt://something:7687").relaxedQuerying().build(), "org.neo4j.ogm.domain.gh651");
        assertThat(sessionFactory.isUseStrictQuerying()).isFalse();

        org.neo4j.ogm.driver.Driver mockedDriver = mock(org.neo4j.ogm.driver.Driver.class);
        when(mockedDriver.getTypeSystem()).thenReturn(TypeSystem.NoNativeTypes.INSTANCE);
        sessionFactory = new SessionFactory(mockedDriver, "org.neo4j.ogm.domain.gh651");
        assertThat(sessionFactory.isUseStrictQuerying()).isTrue();

        sessionFactory = new SessionFactory(mockedDriver, false, "org.neo4j.ogm.domain.gh651");
        assertThat(sessionFactory.isUseStrictQuerying()).isFalse();
    }
}
