package org.neo4j.ogm.drivers.bolt.driver;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DatabaseSelection;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Gerrit Meier
 */
@EnabledIfEnvironmentVariable(named = TestContainersTestBase.SYS_PROPERTY_ACCEPT_AND_USE_COMMERCIAL_EDITION, matches = "yes")
public class BoltDatabaseSelectionTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    @BeforeAll
    public static void setupSessionFactoryAndDatabase() throws Exception {

        try (Session session = getNewBoltConnection().session()) {
            session.run("CREATE DATABASE anotherDatabase").consume();
            Thread.sleep(500); // we need to wait for the database to get created
        }
        try (Session session = getNewBoltConnection().session(SessionConfig.builder().withDatabase("anotherDatabase").build())) {
            session.run("CREATE (n:Node)").consume();
        }

        Configuration ogmConfiguration = getBaseConfigurationBuilder()
            .databaseSelectionProvider(() -> DatabaseSelection.select("anotherDatabase"))
            .build();

        BoltDriver boltOgmDriver = new BoltDriver();
        boltOgmDriver.configure(ogmConfiguration);
        sessionFactory = new SessionFactory(boltOgmDriver, BoltDatabaseSelectionTest.class.getPackage().getName());
    }

    @Test
    public void sessionConfigShouldRespectDatabaseSelection() {
        Iterable<Map<String, Object>> result = sessionFactory.openSession()
            .query("MATCH (n:Node) RETURN n", Collections.emptyMap())
            .queryResults();

        assertThat(result).hasSize(1).allSatisfy(map ->
            assertThat(map).containsKey("n"));

    }

    @AfterAll
    public static void removeDatabase() {
        try (Session session = getNewBoltConnection().session()) {
            session.run("DROP DATABASE anotherDatabase").consume();
        } catch (Exception e) {
            // ignore
        }
    }
}
