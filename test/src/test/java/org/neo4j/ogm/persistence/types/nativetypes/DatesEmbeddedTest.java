package org.neo4j.ogm.persistence.types.nativetypes;

import java.io.File;

import org.assertj.core.util.Files;
import org.junit.BeforeClass;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.session.SessionFactory;

public class DatesEmbeddedTest extends DatesTestBase {

    @BeforeClass
    public static void init() {

        File temporaryFolder = Files.newTemporaryFolder();
        temporaryFolder.deleteOnExit();

        Configuration ogmConfiguration = new Configuration.Builder()
            .uri("file://" + temporaryFolder.getAbsolutePath())
            .useNativeTypes()
            .build();

        EmbeddedDriver driver = new EmbeddedDriver();
        driver.configure(ogmConfiguration);
        sessionFactory = new SessionFactory(driver, SpatialEmbeddedTest.class.getPackage().getName());
    }

}
