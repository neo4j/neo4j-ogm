package org.neo4j.ogm.persistence.types.nativetypes;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Test;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;

public abstract class SpatialTestBase {

    static SessionFactory sessionFactory;

    @AfterClass
    public static void shutDown() {
        sessionFactory.close();
    }

    @Test
    public void convertPersistAndLoadGeographicPoint2d() {
        Session session = sessionFactory.openSession();
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
        Session session = sessionFactory.openSession();
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
        Session session = sessionFactory.openSession();
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
        Session session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        CartesianPoint3d point = new CartesianPoint3d(1, 2, 3);
        spatial.setCartesianPoint3d(point);
        session.save(spatial);

        session.clear();
        SomethingSpatial loaded = session.load(SomethingSpatial.class, spatial.id);
        assertThat(loaded.cartesianPoint3d).isEqualTo(point);
    }

    @Test
    public void convertPersistAndLoadGeographicPoint2dForRelationship() {
        Session session = sessionFactory.openSession();
        SomethingRelationship spatial = new SomethingRelationship();
        GeographicPoint2d point = new GeographicPoint2d(1, 2);
        spatial.setGeographicPoint2d(point);
        session.save(spatial);

        session.clear();
        SomethingRelationship loaded = session.load(SomethingRelationship.class, spatial.id);
        assertThat(loaded.geographicPoint2d).isEqualTo(point);
    }

    @Test
    public void convertPersistAndLoadGeographicPoint3dForRelationship() {
        Session session = sessionFactory.openSession();
        SomethingRelationship spatial = new SomethingRelationship();
        GeographicPoint3d point = new GeographicPoint3d(1, 2, 3);
        spatial.setGeographicPoint3d(point);
        session.save(spatial);

        session.clear();
        SomethingRelationship loaded = session.load(SomethingRelationship.class, spatial.id);
        assertThat(loaded.geographicPoint3d).isEqualTo(point);
    }

    @Test
    public void convertPersistAndLoadCartesianPoint2dForRelationship() {
        Session session = sessionFactory.openSession();
        SomethingRelationship spatial = new SomethingRelationship();
        CartesianPoint2d point = new CartesianPoint2d(1, 2);
        spatial.setCartesianPoint2d(point);
        session.save(spatial);

        session.clear();
        SomethingRelationship loaded = session.load(SomethingRelationship.class, spatial.id);
        assertThat(loaded.cartesianPoint2d).isEqualTo(point);
    }

    @Test
    public void convertPersistAndLoadCartesianPoint3dForRelationship() {
        Session session = sessionFactory.openSession();
        SomethingRelationship spatial = new SomethingRelationship();
        CartesianPoint3d point = new CartesianPoint3d(1, 2, 3);
        spatial.setCartesianPoint3d(point);
        session.save(spatial);

        session.clear();
        SomethingRelationship loaded = session.load(SomethingRelationship.class, spatial.id);
        assertThat(loaded.cartesianPoint3d).isEqualTo(point);
    }


    @NodeEntity
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

        @Relationship("REF")
        private Collection<SomethingRelationship> rels = new ArrayList<>();

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

    @NodeEntity
    static class Some {

        LocalDate localDate;
        private Long id;

        @Relationship(value = "REL", direction = Relationship.INCOMING)
        private Collection<SomethingRelationship> rels = new ArrayList<>();

    }


    @RelationshipEntity("REF")
    static class SomethingRelationship {

        private Long id;

        // Convert to native types
        private GeographicPoint2d geographicPoint2d;
        private GeographicPoint3d geographicPoint3d;

        private CartesianPoint2d cartesianPoint2d;
        private CartesianPoint3d cartesianPoint3d;

        @StartNode
        private SomethingSpatial sometimeStart = new SomethingSpatial();

        @EndNode
        private Some someEnd = new Some();

        public SomethingRelationship() {
            sometimeStart.rels.add(this);
            someEnd.rels.add(this);
        }

        public GeographicPoint2d getGeographicPoint2d() {
            return geographicPoint2d;
        }

        public void setGeographicPoint2d(GeographicPoint2d geographicPoint2d) {
            this.geographicPoint2d = geographicPoint2d;
        }

        public GeographicPoint3d getGeographicPoint3d() {
            return geographicPoint3d;
        }

        public void setGeographicPoint3d(GeographicPoint3d geographicPoint3d) {
            this.geographicPoint3d = geographicPoint3d;
        }

        public CartesianPoint2d getCartesianPoint2d() {
            return cartesianPoint2d;
        }

        public void setCartesianPoint2d(CartesianPoint2d cartesianPoint2d) {
            this.cartesianPoint2d = cartesianPoint2d;
        }

        public CartesianPoint3d getCartesianPoint3d() {
            return cartesianPoint3d;
        }

        public void setCartesianPoint3d(CartesianPoint3d cartesianPoint3d) {
            this.cartesianPoint3d = cartesianPoint3d;
        }
    }

}
