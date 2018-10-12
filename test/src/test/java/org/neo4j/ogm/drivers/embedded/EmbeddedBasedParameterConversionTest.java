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
