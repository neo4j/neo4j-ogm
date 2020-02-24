package org.neo4j.ogm.testutil;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.neo4j.driver.Logging;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.ServerControls;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Neo4jContainer;

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

    private static final String DEFAULT_IMAGE = "neo4j:3.5.12";

    private static final String SYS_PROPERTY_ACCEPT_AND_USE_COMMERCIAL_EDITION = "NEO4J_OGM_NEO4J_ACCEPT_AND_USE_COMMERCIAL_EDITION";

    private static final String SYS_PROPERTY_IMAGE_NAME = "NEO4J_OGM_NEO4J_IMAGE_NAME";

    private static Neo4jContainer neo4jServer;

    private static Configuration.Builder baseConfigurationBuilder;

    static {

        transport = Transport.fromProperties();

        // Bolt and HTTP make use of TestContainers whereas Embedded starts a new embedded instance.
        if (!isEmbeddedDriver()) {

            boolean acceptAndUseCommercialEdition = hasAcceptedAndWantsToUseCommercialEdition();

            String imageName = Optional.ofNullable(System.getenv(SYS_PROPERTY_IMAGE_NAME))
                .orElse(DEFAULT_IMAGE+ (acceptAndUseCommercialEdition ? "-enterprise": ""));

            isEnterpriseEdition = isDockerEnterpriseEdition(imageName);
            version = extractVersionFromDockerImage(imageName);

            neo4jServer = new Neo4jContainer(imageName);

            if (acceptAndUseCommercialEdition) {
                neo4jServer.withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT", "yes");
            }
            neo4jServer.withoutAuthentication().start();

            if (isHttpDriver()){
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

        } else {
            isEnterpriseEdition = isEmbeddedEnterpriseEdition();

            // credentials from TestServer config
            baseConfigurationBuilder = new Configuration.Builder().credentials("neo4j", "password");
            if (isEnterpriseEdition) {
                baseConfigurationBuilder = baseConfigurationBuilder.neo4jConfLocation("classpath:custom-neo4j-ha.conf");
            }
            final ServerControls embedServerControls = new TestHarnessSupplier().get().newServer();
            final Configuration embeddedConfiguration = baseConfigurationBuilder.build();
            driver = new EmbeddedDriver(embedServerControls.graph(), embeddedConfiguration);
            driver.configure(embeddedConfiguration);
            // the embedded driver will take care of the removal of the temporary database directory.

            version = extractVersionFromEmbedded();
        }
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
        return version.compareTo(requiredVersion) >= 0;
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
            Class.forName("org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory", false,
                TestContainersTestBase.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            LOGGER.warn("Could not reliable determine wether HighlyAvailableGraphDatabaseFactory is available or not, assuming not.", e);
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
        List<String> versions = (List<String>) graphDatabaseService.execute("CALL dbms.components() YIELD versions").next().get("versions");
        return versions.get(0).split("-")[0];
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
