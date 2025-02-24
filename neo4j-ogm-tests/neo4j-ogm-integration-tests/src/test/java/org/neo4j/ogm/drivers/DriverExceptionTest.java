/*
 * Copyright (c) 2002-2025 "Neo4j,"
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
package org.neo4j.ogm.drivers;

import static org.junit.jupiter.api.Assertions.assertThrows;


import org.junit.jupiter.api.Test;
import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Luanne Misquitta
 * @see Issue 133
 */
public class DriverExceptionTest {

    @Test
    void shouldThrowExceptionWhenBoltDriverCannotConnect() {
        assertThrows(Exception.class, () -> {
            Configuration configuration = new Configuration.Builder(
                new ClasspathConfigurationSource("ogm-bolt-invalid.properties")).build();
            SessionFactory sessionFactory = new SessionFactory(configuration, "org.neo4j.ogm.domain.social");
            Session session = sessionFactory.openSession();
            session.purgeDatabase();
        });
    }
}
