package org.neo4j.ogm.persistence.types.nativetypes;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.driver.ParameterConversionMode;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.SingleDriverTestClass;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.Coordinate;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;
import org.neo4j.ogm.types.spatial.PointBuilder;

public class SpatialTest extends SingleDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @Before
    public void init() throws IOException {
        Map<String, Object> customConfiguration = Collections
            .singletonMap(ParameterConversionMode.CONFIG_PARAMETER_CONVERSION_MODE,
                ParameterConversionMode.CONVERT_NON_NATIVE_ONLY);
        BoltDriver driver = new BoltDriver(getDriver(), () -> customConfiguration);
        sessionFactory = new SessionFactory(driver, SpatialTest.class.getPackage().getName());
    }

    @After
    public void shutDown() {
        sessionFactory.close();
    }

    @Test
    public void convertAndPersistGeographicPoint2d() {
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
    public void convertAndPersistGeographicPoint3d() {
        session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        spatial.setGeographicPoint3d(new GeographicPoint3d(1,2, 3));
        session.save(spatial);
    }

    @Test
    public void convertAndPersistCartesianPoint2d() {
        session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        spatial.setCartesianPoint2d(new CartesianPoint2d(1,2));
        session.save(spatial);
    }

    @Test
    public void convertAndPersistCartesianPoint3d() {
        session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        spatial.setCartesianPoint3d(new CartesianPoint3d(1,2, 3));
        session.save(spatial);
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

        public SomethingSpatial(){
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
