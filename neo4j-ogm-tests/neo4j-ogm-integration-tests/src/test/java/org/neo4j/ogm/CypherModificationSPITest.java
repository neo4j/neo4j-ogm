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
package org.neo4j.ogm;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.spi.CypherModificationProvider;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;

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

        @Override
        public Function<TransactionManager, BiFunction<Transaction.Type, Iterable<String>, Transaction>> getTransactionFactorySupplier() {
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public Request request(Transaction transaction) {
            return null;
        }

        @Override
        protected String getTypeSystemName() {
            throw new UnsupportedOperationException();
        }
    }

    private static class TestServiceLoaderClassLoader extends ClassLoader {
        TestServiceLoaderClassLoader(ClassLoader originalClassLoader) {
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

