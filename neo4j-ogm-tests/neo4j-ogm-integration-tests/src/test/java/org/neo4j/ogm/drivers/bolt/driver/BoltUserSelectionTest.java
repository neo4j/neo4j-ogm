package org.neo4j.ogm.drivers.bolt.driver;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.SessionConfig;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DatabaseSelection;
import org.neo4j.ogm.config.UserSelection;
import org.neo4j.ogm.persistence.types.nativetypes.DatesBoltTest;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Gerrit Meier
 */
public class BoltUserSelectionTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    @BeforeClass
    public static void setupSessionFactoryAndDatabase() throws Exception {

        try (var session = getJavaDriver().session()) {
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

    @AfterClass
    public static void removeDatabase() {
        try (var session = getJavaDriver().session()) {
            session.run("DROP USER anotherUser").consume();
        }
    }
}
