/*
 * Copyright (c) 2002-2021 "Neo4j,"
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

import org.junit.Test;
import org.mockito.Mockito;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.Driver;

/**
 * @author Michael J. Simons
 * @author Gerrit Meier
 */
public class SessionFactoryTest {

    @Test
    public void correctUseStrictQueryingSettingShouldBeApplied() {

        SessionFactory sessionFactory;

        sessionFactory = new SessionFactory(new Configuration.Builder().uri("bolt://something:7687").build(), "org.neo4j.ogm.domain.gh651");
        assertThat(sessionFactory.isUseStrictQuerying()).isFalse();

        sessionFactory = new SessionFactory(new Configuration.Builder().uri("bolt://something:7687").strictQuerying().build(),
            "org.neo4j.ogm.domain.gh651");
        assertThat(sessionFactory.isUseStrictQuerying()).isTrue();

        Driver mockedDriver = Mockito.mock(Driver.class);
        sessionFactory = new SessionFactory(mockedDriver, "org.neo4j.ogm.domain.gh651");
        assertThat(sessionFactory.isUseStrictQuerying()).isFalse();

        sessionFactory = new SessionFactory(mockedDriver, true, "org.neo4j.ogm.domain.gh651");
        assertThat(sessionFactory.isUseStrictQuerying()).isTrue();
    }
}
