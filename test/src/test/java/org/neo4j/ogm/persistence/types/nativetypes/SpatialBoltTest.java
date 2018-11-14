package org.neo4j.ogm.persistence.types.nativetypes;

import java.net.URI;

import org.junit.BeforeClass;
import org.neo4j.driver.v1.Config.EncryptionLevel;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public class SpatialBoltTest extends SpatialTestBase {

    private static URI boltURI = TestServerBuilders.newInProcessBuilder().newServer().boltURI();

    @BeforeClass
    public static void init() {

        Configuration ogmConfiguration = new Configuration.Builder()
            .uri(boltURI.toString())
            .encryptionLevel(EncryptionLevel.NONE.name())
            .useNativeTypes()
            .build();

        BoltDriver driver = new BoltDriver();
        driver.configure(ogmConfiguration);
        sessionFactory = new SessionFactory(driver, SpatialBoltTest.class.getPackage().getName());
    }
}
