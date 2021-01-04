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
package org.neo4j.ogm.drivers.embedded.driver;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.support.ClassUtils;

/**
 * @author Michael J. Simons
 */
public class EmbeddedDriverTest {

    public static final String NAME_OF_HA_DATABASE_CLASS = "HighlyAvailableGraphDatabase";

    @BeforeClass
    public static void assumeDefaultConfigurationIsDifferentFromCustom() {
        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(new Configuration.Builder().build());
            Result r = getValueOfCypherPlanner(driver.getGraphDatabaseService());
            assumeTrue(r.hasNext());
            assumeTrue("default".equals(r.next().get("value")));
        }
    }

    @Test
    public void shouldHandleCustomConfFiles() {

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(new Configuration.Builder().neo4jConfLocation("classpath:custom-neo4j.conf").build());

            assertCustomConfiguration(driver);
        }
    }

    @Test
    public void shouldHandleCustomConfFilesFromOgmProperties() {

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(
                new Configuration.Builder(new ClasspathConfigurationSource("ogm-pointing-to-custom-conf.properties")).build());

            assertCustomConfiguration(driver);
        }
    }

    @Test
    public void shouldLoadHaBasedOnHaPropertiesFile() {

        assumeTrue(canRunHATests());

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(
                new Configuration.Builder(new ClasspathConfigurationSource("embedded.ha.driver.properties")).build());

            GraphDatabaseService databaseService = driver.getGraphDatabaseService();
            assertThat(databaseService.getClass().getSimpleName()).isEqualTo(NAME_OF_HA_DATABASE_CLASS);
        }
    }

    @Test
    public void shouldLoadHaBasedOnNeo4ConfFile() {

        assumeTrue(canRunHATests());

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(new Configuration.Builder().neo4jConfLocation("classpath:custom-neo4j-ha.conf").build());

            GraphDatabaseService databaseService = driver.getGraphDatabaseService();
            assertThat(databaseService.getClass().getSimpleName()).isEqualTo(NAME_OF_HA_DATABASE_CLASS);
        }
    }

    private static boolean canRunHATests() {
        try {
            Class.forName("org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory", false,
                ClassUtils.getDefaultClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void assertCustomConfiguration(EmbeddedDriver driver) {
        Result r = getValueOfCypherPlanner(driver.getGraphDatabaseService());
        assertThat(r.hasNext()).isTrue();
        assertThat(r.next().get("value")).isEqualTo("COST");
    }

    private static Result getValueOfCypherPlanner(GraphDatabaseService databaseService) {
        return databaseService.execute(""
            + "CALL dbms.listConfig()\n"
            + "YIELD name,  value\n"
            + "WHERE name ='cypher.planner'\n"
            + "RETURN value"
        );
    }
}
