package org.neo4j.ogm.drivers.bolt.driver;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.UserSelection;
import org.neo4j.ogm.persistence.types.nativetypes.DatesBoltTest;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Gerrit Meier
 */
@EnabledIfEnvironmentVariable(named = TestContainersTestBase.SYS_PROPERTY_ACCEPT_AND_USE_COMMERCIAL_EDITION, matches = "yes")
public class BoltUserSelectionTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    @BeforeAll
    public static void setupSessionFactoryAndDatabase() throws Exception {

        try (var session = getNewBoltConnection().session()) {
            session.run("CREATE USER anotherUser SET PASSWORD 'blubb'").consume();
            Thread.sleep(500);
        }

        Configuration ogmConfiguration = getBaseConfigurationBuilder()
            .userSelectionProvider(() -> UserSelection.impersonate("anotherUser"))
            .build();

        BoltDriver boltOgmDriver = new BoltDriver();
        boltOgmDriver.configure(ogmConfiguration);
        sessionFactory = new SessionFactory(boltOgmDriver, DatesBoltTest.class.getPackage().getName());
    }

    @Test
    public void sessionConfigShouldRespectDatabaseSelection() {
        var result = sessionFactory.openSession()
            .query("SHOW CURRENT user YIELD user", Map.of())
            .queryResults();

        assertThat(result).hasSize(1).allSatisfy(map ->
            assertThat(map).containsEntry("user", "anotherUser"));

    }

    @AfterAll
    public static void removeDatabase() {
        try (var session = getNewBoltConnection().session()) {
            session.run("DROP USER anotherUser").consume();
        }
    }
}
