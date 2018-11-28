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
package org.neo4j.ogm.drivers.embedded.driver;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.*;

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.security.WriteOperationsNotAllowedException;
import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.support.ClassUtils;

/**
 * @author Michael J. Simons
 */
public class EmbeddedDriverTest {

    public static final String NAME_OF_HA_DATABASE_CLASS = "HighlyAvailableGraphDatabase";

    @Test
    public void shouldHandleCustomConfFiles() {

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(new Configuration.Builder().neo4jConfLocation("classpath:custom-neo4j.conf").build());

            GraphDatabaseService databaseService = driver.getGraphDatabaseService();

            assertReadOnly(databaseService);
        }
    }

    @Test
    public void shouldHandleCustomConfFilesFromOgmProperties() {

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(
                new Configuration.Builder(new ClasspathConfigurationSource("ogm-pointing-to-custom-conf.properties")).build());

            GraphDatabaseService databaseService = driver.getGraphDatabaseService();

            assertReadOnly(databaseService);
        }
    }

    private static void assertReadOnly(GraphDatabaseService databaseService) {
        Result r = databaseService.execute("MATCH (n) RETURN n");
        assertThat(r.hasNext()).isFalse();

        try (Transaction tx = databaseService.beginTx()) {
            // The config sets dbms.read_only = true
            assertThatExceptionOfType(WriteOperationsNotAllowedException.class).isThrownBy(() -> {
                databaseService.execute("CREATE (n: Node {name: 'node'})");
            });
            tx.failure();
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

    static boolean canRunHATests() {
        try {
            Class.forName("org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory", false,
                ClassUtils.getDefaultClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
