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
package org.neo4j.ogm.testutil;

import java.util.Locale;
import java.util.Optional;

import org.neo4j.driver.AccessMode;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Logging;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Value;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.TestcontainersConfiguration;

/**
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public class TestContainersTestBase {

    public static final Logger LOGGER = LoggerFactory.getLogger(TestContainersTestBase.class);

    public static final Driver driver;

    public static final String version;

    public static final String DEFAULT_IMAGE = "neo4j:5.26.8";

    public static final String SYS_PROPERTY_ACCEPT_AND_USE_COMMERCIAL_EDITION = "NEO4J_OGM_NEO4J_ACCEPT_AND_USE_COMMERCIAL_EDITION";

    public static final String SYS_PROPERTY_IMAGE_NAME = "NEO4J_OGM_NEO4J_IMAGE_NAME";

    public static final String SYS_PROPERTY_NEO4J_URL = "NEO4J_OGM_NEO4J_URL";

    public static final String SYS_PROPERTY_NEO4J_PASSWORD = "NEO4J_OGM_NEO4J_PASSWORD";

    public static final String DEFAULT_PASSWORD = "verysecret";

    public static Neo4jContainer neo4jServer;

    public static Configuration.Builder baseConfigurationBuilder;

    public static final String NEO4J_URL = Optional.ofNullable(System.getenv(SYS_PROPERTY_NEO4J_URL)).orElse("");

    public static final String NEO4J_PASSWORD = Optional.ofNullable(System.getenv(SYS_PROPERTY_NEO4J_PASSWORD))
        .orElse("").trim();

    static {

        boolean acceptAndUseCommercialEdition = hasAcceptedAndWantsToUseCommercialEdition();

        if (!(NEO4J_URL.isEmpty() || NEO4J_PASSWORD.isEmpty())) {
            LOGGER.info("Using Neo4j instance at {}.", NEO4J_URL);
            driver = new BoltDriver();
            baseConfigurationBuilder = new Configuration.Builder()
                .uri(NEO4J_URL)
                .verifyConnection(true)
                .withCustomProperty(BoltDriver.CONFIG_PARAMETER_BOLT_LOGGING, Logging.slf4j())
                .credentials("neo4j", NEO4J_PASSWORD);
            driver.configure(baseConfigurationBuilder.build());
            version = extractVersionFromBolt();
        } else {
            LOGGER.info("Using Neo4j test container.");
            String imageName = Optional.ofNullable(System.getenv(SYS_PROPERTY_IMAGE_NAME))
                .orElse(DEFAULT_IMAGE + (acceptAndUseCommercialEdition ? "-enterprise" : ""));

            version = extractVersionFromDockerImage(imageName);

            boolean containerReuseSupported = TestcontainersConfiguration
                .getInstance().environmentSupportsReuse();
            boolean disableContainerReuse = Boolean.getBoolean("disableContainerReuse");
            if (disableContainerReuse) {
                LOGGER.warn("Reuse is explicitly disable");
            }
            neo4jServer = new Neo4jContainer<>(imageName)
                .waitingFor(Neo4jContainer.WAIT_FOR_BOLT)
                .withReuse(containerReuseSupported && !disableContainerReuse);
            if (acceptAndUseCommercialEdition) {
                neo4jServer.withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT", "yes");
            }
            neo4jServer
                .withAdminPassword(DEFAULT_PASSWORD)
                .start();
            driver = new BoltDriver();

            baseConfigurationBuilder = new Configuration.Builder()
                .uri(neo4jServer.getBoltUrl())
                .credentials("neo4j", DEFAULT_PASSWORD)
                .verifyConnection(true)
                .withCustomProperty(BoltDriver.CONFIG_PARAMETER_BOLT_LOGGING, Logging.slf4j());

            driver.configure(baseConfigurationBuilder.build());

            Runtime.getRuntime().addShutdownHook(new Thread(neo4jServer::stop));
        }
    }

    protected static org.neo4j.driver.Driver getNewBoltConnection() {

        if (neo4jServer != null) {
            return GraphDatabase.driver(neo4jServer.getBoltUrl(), AuthTokens.basic("neo4j", DEFAULT_PASSWORD));
        } else {
            return GraphDatabase.driver(NEO4J_URL, AuthTokens.basic("neo4j", NEO4J_PASSWORD));
        }
    }

    public static boolean hasAcceptedAndWantsToUseCommercialEdition() {
        return Optional.ofNullable(
            System.getenv(TestContainersTestBase.SYS_PROPERTY_ACCEPT_AND_USE_COMMERCIAL_EDITION))
            .orElse("no").toLowerCase(Locale.ENGLISH).equals("yes");
    }

    public static Driver getDriver() {
        return driver;
    }

    protected static Configuration.Builder getBaseConfigurationBuilder() {
        return Configuration.Builder.copy(baseConfigurationBuilder);
    }

    protected static boolean isVersionOrGreater(String requiredVersion) {
        if (version.equals(requiredVersion)) {
            return true;
        }
        String[] serverVersionParts = version.split("-drop")[0].split("\\.");
        String[] requiredVersionParts = requiredVersion.split("\\.");
        int length = Math.max(serverVersionParts.length, requiredVersionParts.length);

        for(int i = 0; i < length; i++) {
            int serverVersionPart = i < serverVersionParts.length ? Integer.parseInt(serverVersionParts[i]) : 0;
            int requiredVersionPart = i < requiredVersionParts.length ? Integer.parseInt(requiredVersionParts[i]) : 0;
            if(serverVersionPart < requiredVersionPart) {
                return false;
            } else if (i == 0 && serverVersionPart > requiredVersionPart) {
                return true;
            }
        }
        return true;
    }

    private static String extractVersionFromDockerImage(String imageName) {
        return imageName.replace("neo4j:", "").replace("neo4j/neo4j-experimental:", "").replace("-enterprise", "");
    }

    private static String extractVersionFromBolt() {

        org.neo4j.driver.Driver driver = getDriver().unwrap(org.neo4j.driver.Driver.class);

        String version;
        SessionConfig sessionConfig = SessionConfig.builder().withDefaultAccessMode(AccessMode.READ).build();
        try (Session session = driver.session(sessionConfig)) {
            version = session.run("CALL dbms.components() YIELD versions").single().get("versions").asList(
                Value::asString).get(0);
        }
        return version.toLowerCase(Locale.ENGLISH);
    }

}
