package org.neo4j.ogm.persistence.types.nativetypes;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.driver.ParameterConversionMode;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;

public class SpatialTest {

    private static final Config DRIVER_CONFIG = Config.build().withoutEncryption().toConfig();

    private static final Map<String, Object> customConfiguration = Collections
        .singletonMap(ParameterConversionMode.CONFIG_PARAMETER_CONVERSION_MODE,
            ParameterConversionMode.CONVERT_NON_NATIVE_ONLY);

    private static URI boltURI = TestServerBuilders.newInProcessBuilder().newServer().boltURI();

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void init() {
        BoltDriver driver = new BoltDriver(GraphDatabase.driver(boltURI, DRIVER_CONFIG), () -> customConfiguration);
        sessionFactory = new SessionFactory(driver, SpatialTest.class.getPackage().getName());
    }

    @AfterClass
    public static void shutDown() {
        sessionFactory.close();
    }

    @Test
    public void convertPersistAndLoadGeographicPoint2d() {
        session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        GeographicPoint2d point = new GeographicPoint2d(1, 2);
        spatial.setGeographicPoint2d(point);
        session.save(spatial);

        session.clear();
        SomethingSpatial loaded = session.load(SomethingSpatial.class, spatial.id);
        assertThat(loaded.geographicPoint2d).isEqualTo(point);
    }

    @Test
    public void convertPersistAndLoadGeographicPoint3d() {
        session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        GeographicPoint3d point = new GeographicPoint3d(1, 2, 3);
        spatial.setGeographicPoint3d(point);
        session.save(spatial);

        session.clear();
        SomethingSpatial loaded = session.load(SomethingSpatial.class, spatial.id);
        assertThat(loaded.geographicPoint3d).isEqualTo(point);
    }

    @Test
    public void convertPersistAndLoadCartesianPoint2d() {
        session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        CartesianPoint2d point = new CartesianPoint2d(1, 2);
        spatial.setCartesianPoint2d(point);
        session.save(spatial);

        session.clear();
        SomethingSpatial loaded = session.load(SomethingSpatial.class, spatial.id);
        assertThat(loaded.cartesianPoint2d).isEqualTo(point);
    }

    @Test
    public void convertPersistAndLoadCartesianPoint3d() {
        session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        CartesianPoint3d point = new CartesianPoint3d(1, 2, 3);
        spatial.setCartesianPoint3d(point);
        session.save(spatial);

        session.clear();
        SomethingSpatial loaded = session.load(SomethingSpatial.class, spatial.id);
        assertThat(loaded.cartesianPoint3d).isEqualTo(point);
    }

    static class SomethingSpatial {

        private Long id;

        // Convert to native types
        private GeographicPoint2d geographicPoint2d;
        private GeographicPoint3d geographicPoint3d;

        private CartesianPoint2d cartesianPoint2d;
        private CartesianPoint3d cartesianPoint3d;

        // Do not try to convert
        private List<String> stringList = new ArrayList<>();

        @Properties
        private Map<String, String> properties = new HashMap<>();

        public SomethingSpatial() {
            stringList.add("a");
            stringList.add("b");

            properties.put("a", "a");
            properties.put("b", "b");
        }

        public void setGeographicPoint2d(GeographicPoint2d geographicPoint2d) {
            this.geographicPoint2d = geographicPoint2d;
        }

        public void setGeographicPoint3d(GeographicPoint3d geographicPoint3d) {
            this.geographicPoint3d = geographicPoint3d;
        }

        public void setCartesianPoint2d(CartesianPoint2d cartesianPoint2d) {
            this.cartesianPoint2d = cartesianPoint2d;
        }

        public void setCartesianPoint3d(CartesianPoint3d cartesianPoint3d) {
            this.cartesianPoint3d = cartesianPoint3d;
        }
    }
}
