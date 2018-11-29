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

package org.neo4j.ogm;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.spi.CypherModificationProvider;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Michael J. Simons
 */
public class CypherModificationSPITest {
    @Test
    public void abstractDriverShouldLoadCypherModificationsInCorrectOrder() {

        Thread currentThread = Thread.currentThread();

        ClassLoader originalClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(new TestServiceLoaderClassLoader(originalClassLoader));

        Configuration driverConfiguration = new Configuration.Builder()
            .withCustomProperty("config1", 6)
            .withCustomProperty("config2", 9)
            .build();

        Driver driver = new TestDriver();
        driver.configure(driverConfiguration);

        try {
            Function<String, String> cypherModification = driver.getCypherModification();
            assertThat(cypherModification.apply("What do you get if you multiply six by nine?"))
                .isEqualTo("42");
        } finally {
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }

    @Test
    public void driverShouldProvideNoopModificationWithoutAnyProvider() {

        Driver driver = new TestDriver();
        driver.configure(new Configuration.Builder().build());

        Function<String, String> cypherModification = driver.getCypherModification();
        String cypher = "MATCH (n) RETURN n";
        assertThat(cypherModification.apply(cypher)).isEqualTo(cypher);
    }

    @Test
    public void driverShouldNotProvideModificationWithoutConfiguration() {

        Driver driver = new TestDriver();
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(driver::getCypherModification)
            .withMessage("Driver is not configured and cannot load Cypher modifications.")
            .withNoCause();
    }

    // The providers need to be public, don't change that.

    public static class CypherModificationProvider1 implements CypherModificationProvider {

        @Override
        public int getOrder() {
            return 20;
        }

        @Override
        public Function<String, String> getCypherModification(Map<String, Object> configuration) {
            Integer value1 = (Integer) configuration.get("config1");
            Integer value2 = (Integer) configuration.get("config2");
            return modifiedCypher -> modifiedCypher.replaceAll("theAnswer", Integer.toString(value1 * value2, 13));
        }
    }

    public static class CypherModificationProvider2 implements CypherModificationProvider {

        @Override
        public int getOrder() {
            return 10;
        }

        @Override
        public Function<String, String> getCypherModification(Map<String, Object> configuration) {
            return originalCypher -> "theAnswer";
        }
    }

    private static class TestDriver extends AbstractConfigurableDriver {
        // Not interested in any of those.
        @Override
        public Transaction newTransaction(Transaction.Type type, Iterable<String> bookmarks) {
            return null;
        }

        @Override
        public void close() {
        }

        @Override public Request request() {
            return null;
        }

        @Override
        protected String getTypeSystemName() {
            throw new UnsupportedOperationException();
        }
    }

    private static class TestServiceLoaderClassLoader extends ClassLoader {
        public TestServiceLoaderClassLoader(ClassLoader originalClassLoader) {
            super(originalClassLoader);
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            // Don't think too long about that approach to push stuff into Javas SPI.
            if ("META-INF/services/org.neo4j.ogm.spi.CypherModificationProvider".equals(name)) {
                return Collections.enumeration(Arrays.asList(
                    super.getResource("spi/cypher_modification1"),
                    super.getResource("spi/cypher_modification2")));
            } else {
                return super.getResources(name);
            }
        }
    }
}

