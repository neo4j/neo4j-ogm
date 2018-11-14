package org.neo4j.ogm.persistence.types.nativetypes;

import java.net.URI;

import org.junit.BeforeClass;
import org.neo4j.driver.v1.Config;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.SessionFactory;

public class DatesBoltTest extends DatesTestBase {

    private static URI boltURI = TestServerBuilders.newInProcessBuilder().newServer().boltURI();

    @BeforeClass
    public static void init() {

        Configuration ogmConfiguration = new Configuration.Builder()
            .uri(boltURI.toString())
            .encryptionLevel(Config.EncryptionLevel.NONE.name())
            .useNativeTypes()
            .build();

        BoltDriver driver = new BoltDriver();
        driver.configure(ogmConfiguration);
        sessionFactory = new SessionFactory(driver, DatesBoltTest.class.getPackage().getName());
    }
}
