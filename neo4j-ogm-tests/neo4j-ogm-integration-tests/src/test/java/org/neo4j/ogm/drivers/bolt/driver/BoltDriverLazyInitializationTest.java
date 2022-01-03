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
package org.neo4j.ogm.drivers.bolt.driver;

import org.junit.Before;
import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DriverLazyInitializationTest;
import org.neo4j.ogm.testutil.TestUtils;

/**
 * @author Frantisek Hartman
 */
public class BoltDriverLazyInitializationTest extends DriverLazyInitializationTest {

    @Before
    public void setUp() {
        configBuilder = new Configuration.Builder(new ClasspathConfigurationSource("ogm-bolt.properties"))
            .uri("bolt://localhost:" + TestUtils.getAvailablePort());
    }
}
