/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.drivers.embedded;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.*;
import static org.neo4j.ogm.driver.ParameterConversionMode.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.testutil.SingleDriverTestClass;

/**
 * @author Michael J. Simons
 */
public class EmbeddedBasedParameterConversionTest extends SingleDriverTestClass {

    @Test
    public void shouldUseNativeTypesWhenNonNativeTypesOnlyIsActive() {

        assumeTrue(databaseSupportJava8TimeTypes());

        Map<String, Object> customConfiguration = new HashMap<>();
        customConfiguration.put(CONFIG_PARAMETER_CONVERSION_MODE, CONVERT_NON_NATIVE_ONLY);

        GraphDatabaseService graphDatabaseService = getGraphDatabaseService();
        EmbeddedDriver embeddedOgmDriver = new EmbeddedDriver(graphDatabaseService, () -> customConfiguration);

        doWithSessionFactoryOf(embeddedOgmDriver, new Class[] { EmbeddedBasedParameterConversionTest.class },
            sessionFactory -> {
                Session session = sessionFactory.openSession();

                LocalDateTime originalDateTime = LocalDateTime.of(2018, 10, 11, 15, 24);

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("createdAt", originalDateTime);
                session.query("CREATE (n:Test {createdAt: $createdAt})", parameters);

                Object createdAt = graphDatabaseService.execute("MATCH (n:Test) RETURN n.createdAt AS createdAt").next()
                    .get("createdAt");
                assertThat(createdAt).isInstanceOf(LocalDateTime.class)
                    .isEqualTo(originalDateTime);
            });
    }
}
