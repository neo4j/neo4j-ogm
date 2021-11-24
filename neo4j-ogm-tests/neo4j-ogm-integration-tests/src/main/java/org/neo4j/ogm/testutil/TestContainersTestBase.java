package org.neo4j.ogm.testutil;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.neo4j.driver.AccessMode;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Logging;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Value;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.Neo4j;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.TestcontainersConfiguration;

/**
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public class TestContainersTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestContainersTestBase.class);

    private static final Driver driver;

    private static final Transport transport;

    private static final String version;

    private static final boolean isEnterpriseEdition;

    private static final String DEFAULT_IMAGE = "neo4j:4.3.7";

    private static final String SYS_PROPERTY_ACCEPT_AND_USE_COMMERCIAL_EDITION = "NEO4J_OGM_NEO4J_ACCEPT_AND_USE_COMMERCIAL_EDITION";

    private static final String SYS_PROPERTY_IMAGE_NAME = "NEO4J_OGM_NEO4J_IMAGE_NAME";

    private static final String SYS_PROPERTY_NEO4J_URL = "NEO4J_OGM_NEO4J_URL";

    private static final String SYS_PROPERTY_NEO4J_PASSWORD = "NEO4J_OGM_NEO4J_PASSWORD";

    private static Neo4jContainer neo4jServer;

    private static Configuration.Builder baseConfigurationBuilder;

    static {

        transport = Transport.fromProperties();

        // Bolt and HTTP make use of TestContainers whereas Embedded starts a new embedded instance.
        if (!isEmbeddedDriver()) {

            boolean acceptAndUseCommercialEdition = hasAcceptedAndWantsToUseCommercialEdition();

            String neo4jUrl = Optional.ofNullable(System.getenv(SYS_PROPERTY_NEO4J_URL)).orElse("");
            String neo4jPassword = Optional.ofNullable(System.getenv(SYS_PROPERTY_NEO4J_PASSWORD)).orElse("").trim();
            if (!(neo4jUrl.isEmpty() || neo4jPassword.isEmpty()) && isBoltDriver()) {
                LOGGER.info("Using Neo4j instance at {}.", neo4jUrl);
                driver = new BoltDriver();
                baseConfigurationBuilder = new Configuration.Builder()
                    .uri(neo4jUrl)
                    .verifyConnection(true)
                    .withCustomProperty(BoltDriver.CONFIG_PARAMETER_BOLT_LOGGING, Logging.slf4j())
                    .credentials("neo4j", neo4jPassword);
                driver.configure(baseConfigurationBuilder.build());
                version = extractVersionFromBolt();
                isEnterpriseEdition = Arrays.asList("commercial", "enterprise").contains(extractEditionFromBolt());
            } else {
                LOGGER.info("Using Neo4j test container.");
                String imageName = Optional.ofNullable(System.getenv(SYS_PROPERTY_IMAGE_NAME))
                    .orElse(DEFAULT_IMAGE + (acceptAndUseCommercialEdition ? "-enterprise" : ""));

                isEnterpriseEdition = isDockerEnterpriseEdition(imageName);
                version = extractVersionFromDockerImage(imageName);

                boolean containerReuseSupported = TestcontainersConfiguration
                    .getInstance().environmentSupportsReuse();
                neo4jServer = new Neo4jContainer<>(imageName)
                    .withReuse(containerReuseSupported);

                if (acceptAndUseCommercialEdition) {
                    neo4jServer.withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT", "yes");
                }
                neo4jServer.withoutAuthentication().start();

                if (isHttpDriver()) {
                    driver = new HttpDriver();

                    baseConfigurationBuilder = new Configuration.Builder()
                        .uri(neo4jServer.getHttpUrl());

                    driver.configure(baseConfigurationBuilder.build());

                } else {
                    driver = new BoltDriver();

                    baseConfigurationBuilder = new Configuration.Builder()
                        .uri(neo4jServer.getBoltUrl())
                        .verifyConnection(true)
                        .withCustomProperty(BoltDriver.CONFIG_PARAMETER_BOLT_LOGGING, Logging.slf4j());

                    driver.configure(baseConfigurationBuilder.build());
                }
                Runtime.getRuntime().addShutdownHook(new Thread(neo4jServer::stop));
            }

        } else {
            isEnterpriseEdition = isEmbeddedEnterpriseEdition();

            // credentials from TestServer config
            baseConfigurationBuilder = new Configuration.Builder().credentials("neo4j", "password");
            final Neo4j embedServerControls = new TestHarnessSupplier().get().build();
            final Configuration embeddedConfiguration = baseConfigurationBuilder.build();
            driver = new EmbeddedDriver(embedServerControls.defaultDatabaseService(), embeddedConfiguration);
            driver.configure(embeddedConfiguration);
            // the embedded driver will take care of the removal of the temporary database directory.

            version = extractVersionFromEmbedded();
        }
    }

    protected static org.neo4j.driver.Driver getBoltConnection() {

        if (neo4jServer != null) {
            return GraphDatabase.driver(neo4jServer.getBoltUrl(), AuthTokens.none());
        }
        throw new IllegalStateException("Bolt connection can only be provided into a test container.");
    }

    private static boolean hasAcceptedAndWantsToUseCommercialEdition() {
        return Optional.ofNullable(
            System.getenv(TestContainersTestBase.SYS_PROPERTY_ACCEPT_AND_USE_COMMERCIAL_EDITION))
            .orElse("no").toLowerCase(Locale.ENGLISH).equals("yes");
    }

    protected static Driver getDriver() {
        return driver;
    }

    protected static String getBoltUrl() {
        return neo4jServer.getBoltUrl();
    }

    protected static Configuration.Builder getBaseConfigurationBuilder() {
        return Configuration.Builder.copy(baseConfigurationBuilder);
    }

    protected static boolean useEnterpriseEdition() {
        return isEnterpriseEdition;
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

    protected static boolean isBoltDriver() {
        return transport == Transport.BOLT;
    }

    protected static boolean isHttpDriver() {
        return transport == Transport.HTTP;
    }

    protected static boolean isEmbeddedDriver() {
        return transport == Transport.EMBEDDED;
    }

    private static boolean isDockerEnterpriseEdition(String imageName) {
        return imageName.endsWith("enterprise");
    }

    private static boolean isEmbeddedEnterpriseEdition() {
        try {
            Class.forName("com.neo4j.dbms.api.ClusterDatabaseManagementServiceFactory", false, TestContainersTestBase.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            LOGGER.warn("Could not reliable determine weather HighlyAvailableGraphDatabaseFactory is available or not, assuming not.", e);
            return false;
        }
    }

    private static String extractVersionFromDockerImage(String imageName) {
        return imageName.replace("neo4j:", "").replace("neo4j/neo4j-experimental:", "").replace("-enterprise", "");
    }

    private static String extractVersionFromEmbedded() {
        if (transport != Transport.EMBEDDED) {
            throw new IllegalStateException("Cannot extract embedded version from non-embedded instance.");
        }
        EmbeddedDriver embeddedDriver = (EmbeddedDriver) getDriver();
        GraphDatabaseService graphDatabaseService = embeddedDriver.unwrap(GraphDatabaseService.class);
        List<String> versions = graphDatabaseService.executeTransactionally(
            "CALL dbms.components() YIELD versions", Map.of(), result -> (List<String>) result.next().get("versions"));
        return versions.get(0).split("-")[0];
    }

    private static String extractVersionFromBolt() {

        if (transport != Transport.BOLT) {
            throw new IllegalStateException("Cannot extract bolt version from non-bolt instance.");
        }
        org.neo4j.driver.Driver driver = getDriver().unwrap(org.neo4j.driver.Driver.class);

        String version;
        SessionConfig sessionConfig = SessionConfig.builder().withDefaultAccessMode(AccessMode.READ).build();
        try (Session session = driver.session(sessionConfig)) {
            version = session.run("CALL dbms.components() YIELD versions").single().get("versions").asList(
                Value::asString).get(0);
        }
        return version.toLowerCase(Locale.ENGLISH);
    }

    private static String extractEditionFromBolt() {

        if (transport != Transport.BOLT) {
            throw new IllegalStateException("Cannot extract bolt version from non-bolt instance.");
        }
        org.neo4j.driver.Driver driver = getDriver().unwrap(org.neo4j.driver.Driver.class);

        String edition;
        SessionConfig sessionConfig = SessionConfig.builder().withDefaultAccessMode(AccessMode.READ).build();
        try (Session session = driver.session(sessionConfig)) {
            edition = session.run("call dbms.components() yield edition").single().get("edition").asString();
        }
        return edition.toLowerCase(Locale.ENGLISH);
    }

    private enum Transport {
        HTTP,
        BOLT,
        EMBEDDED;

        private static Transport fromProperties() {
            // Default to the maven profiles from the past.
            String configFileName = System.getProperty("ogm.properties");
            if (configFileName == null) {
                return BOLT;
            }
            switch (configFileName) {
                case "ogm-http.properties":
                    return HTTP;
                case "ogm-embedded.properties":
                    return EMBEDDED;
                case "ogm-bolt.properties":
                default:
                    return BOLT;
            }

        }
    }

}
