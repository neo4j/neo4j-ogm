package org.neo4j.ogm.persistence.types.nativetypes.filter;

import static org.assertj.core.api.Assertions.*;
import static org.neo4j.ogm.cypher.function.NativeDistanceComparison.*;

import java.util.Collection;

import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.function.DistanceFromNativePoint;
import org.neo4j.ogm.persistence.types.nativetypes.SomethingSpatial;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;

abstract class DistanceComparisonTestBase {

    static SessionFactory sessionFactory;

    @Test
    public void filterForCartesianPoint2d() {
        Session session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        CartesianPoint2d point = new CartesianPoint2d(1, 2);
        spatial.setCartesianPoint2d(point);
        session.save(spatial);

        DistanceFromNativePoint distanceFromNativePoint = new DistanceFromNativePoint(new CartesianPoint2d(2, 2), 2);
        Filter filter = new Filter("cartesianPoint2d", distanceComparisonFor(distanceFromNativePoint),
            ComparisonOperator.LESS_THAN);
        filter.setOwnerEntityType(SomethingSpatial.class);

        Collection<SomethingSpatial> somethingSpatials = session.loadAll(SomethingSpatial.class, filter);

        assertThat(somethingSpatials).hasSize(1);
    }

    @Test
    public void filterForCartesianPoint2dNoMatch() {
        Session session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        CartesianPoint2d point = new CartesianPoint2d(1, 2);
        spatial.setCartesianPoint2d(point);
        session.save(spatial);

        DistanceFromNativePoint distanceFromNativePoint = new DistanceFromNativePoint(new CartesianPoint2d(2, 2), 1);
        Filter filter = new Filter("cartesianPoint2d", distanceComparisonFor(distanceFromNativePoint),
            ComparisonOperator.LESS_THAN);
        filter.setOwnerEntityType(SomethingSpatial.class);

        Collection<SomethingSpatial> somethingSpatials = session.loadAll(SomethingSpatial.class, filter);

        assertThat(somethingSpatials).hasSize(0);
    }

    @Test
    public void filterForCartesianPoint3d() {
        Session session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        CartesianPoint3d point = new CartesianPoint3d(1, 2, 3);
        spatial.setCartesianPoint3d(point);
        session.save(spatial);

        DistanceFromNativePoint distanceFromNativePoint = new DistanceFromNativePoint(new CartesianPoint3d(2, 2, 3), 2);
        Filter filter = new Filter("cartesianPoint3d", distanceComparisonFor(distanceFromNativePoint),
            ComparisonOperator.LESS_THAN);
        filter.setOwnerEntityType(SomethingSpatial.class);

        Collection<SomethingSpatial> somethingSpatials = session.loadAll(SomethingSpatial.class, filter);

        assertThat(somethingSpatials).hasSize(1);
    }

    @Test
    public void filterForCartesianPoint3dNoMatch() {
        Session session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        CartesianPoint3d point = new CartesianPoint3d(1, 2, 3);
        spatial.setCartesianPoint3d(point);
        session.save(spatial);

        DistanceFromNativePoint distanceFromNativePoint = new DistanceFromNativePoint(new CartesianPoint3d(2, 2, 3), 1);
        Filter filter = new Filter("cartesianPoint3d", distanceComparisonFor(distanceFromNativePoint),
            ComparisonOperator.LESS_THAN);
        filter.setOwnerEntityType(SomethingSpatial.class);

        Collection<SomethingSpatial> somethingSpatials = session.loadAll(SomethingSpatial.class, filter);

        assertThat(somethingSpatials).hasSize(0);
    }

    @Test
    public void filterForGeographicPoint2d() {
        Session session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        GeographicPoint2d centralStationLocation = new GeographicPoint2d(55.6093093, 13.0004377);

        spatial.setGeographicPoint2d(centralStationLocation);
        session.save(spatial);

        GeographicPoint2d office = new GeographicPoint2d(55.611851, 12.9949028);
        DistanceFromNativePoint distanceFromNativePoint = new DistanceFromNativePoint(office, 449);
        Filter filter = new Filter("geographicPoint2d", distanceComparisonFor(distanceFromNativePoint),
            ComparisonOperator.LESS_THAN);
        filter.setOwnerEntityType(SomethingSpatial.class);

        Collection<SomethingSpatial> somethingSpatials = session.loadAll(SomethingSpatial.class, filter);

        assertThat(somethingSpatials).hasSize(1);
    }

    @Test
    public void filterForGeographicPoint2dNoMatch() {
        Session session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        GeographicPoint2d centralStationLocation = new GeographicPoint2d(55.6093093, 13.0004377);

        spatial.setGeographicPoint2d(centralStationLocation);
        session.save(spatial);

        GeographicPoint2d office = new GeographicPoint2d(55.611851, 12.9949028);
        DistanceFromNativePoint distanceFromNativePoint = new DistanceFromNativePoint(office, 448);
        Filter filter = new Filter("geographicPoint2d", distanceComparisonFor(distanceFromNativePoint),
            ComparisonOperator.LESS_THAN);
        filter.setOwnerEntityType(SomethingSpatial.class);

        Collection<SomethingSpatial> somethingSpatials = session.loadAll(SomethingSpatial.class, filter);

        assertThat(somethingSpatials).hasSize(0);
    }

    @Test
    public void filterForGeographicPoint3d() {
        Session session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        GeographicPoint3d centralStationLocation = new GeographicPoint3d(55.6093093, 13.0004377, -5);

        spatial.setGeographicPoint3d(centralStationLocation);
        session.save(spatial);

        GeographicPoint3d office = new GeographicPoint3d(55.611851, 12.9949028, 15);
        DistanceFromNativePoint distanceFromNativePoint = new DistanceFromNativePoint(office, 448.9591);
        Filter filter = new Filter("geographicPoint3d", distanceComparisonFor(distanceFromNativePoint),
            ComparisonOperator.LESS_THAN);
        filter.setOwnerEntityType(SomethingSpatial.class);

        Collection<SomethingSpatial> somethingSpatials = session.loadAll(SomethingSpatial.class, filter);

        assertThat(somethingSpatials).hasSize(1);
    }

    @Test
    public void filterForGeographicPoint3dNoMatch() {
        Session session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        GeographicPoint3d centralStationLocation = new GeographicPoint3d(55.6093093, 13.0004377, -5);

        spatial.setGeographicPoint3d(centralStationLocation);
        session.save(spatial);

        GeographicPoint3d office = new GeographicPoint3d(55.611851, 12.9949028, 15);
        DistanceFromNativePoint distanceFromNativePoint = new DistanceFromNativePoint(office, 448.950);
        Filter filter = new Filter("geographicPoint3d", distanceComparisonFor(distanceFromNativePoint),
            ComparisonOperator.LESS_THAN);
        filter.setOwnerEntityType(SomethingSpatial.class);

        Collection<SomethingSpatial> somethingSpatials = session.loadAll(SomethingSpatial.class, filter);

        assertThat(somethingSpatials).hasSize(0);
    }
}
