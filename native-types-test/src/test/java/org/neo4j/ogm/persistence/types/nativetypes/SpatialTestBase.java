/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.persistence.types.nativetypes;

import static org.assertj.core.api.Assertions.*;

import org.junit.AfterClass;
import org.junit.Test;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;

/**
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
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
        SomethingSpatial loaded = session.load(SomethingSpatial.class, spatial.getId());
        assertThat(loaded.getGeographicPoint2d()).isEqualTo(point);
    }

    @Test
    public void convertPersistAndLoadGeographicPoint3d() {
        Session session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        GeographicPoint3d point = new GeographicPoint3d(1, 2, 3);
        spatial.setGeographicPoint3d(point);
        session.save(spatial);

        session.clear();
        SomethingSpatial loaded = session.load(SomethingSpatial.class, spatial.getId());
        assertThat(loaded.getGeographicPoint3d()).isEqualTo(point);
    }

    @Test
    public void convertPersistAndLoadCartesianPoint2d() {
        Session session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        CartesianPoint2d point = new CartesianPoint2d(1, 2);
        spatial.setCartesianPoint2d(point);
        session.save(spatial);

        session.clear();
        SomethingSpatial loaded = session.load(SomethingSpatial.class, spatial.getId());
        assertThat(loaded.getCartesianPoint2d()).isEqualTo(point);
    }

    @Test
    public void convertPersistAndLoadCartesianPoint3d() {
        Session session = sessionFactory.openSession();
        SomethingSpatial spatial = new SomethingSpatial();
        CartesianPoint3d point = new CartesianPoint3d(1, 2, 3);
        spatial.setCartesianPoint3d(point);
        session.save(spatial);

        session.clear();
        SomethingSpatial loaded = session.load(SomethingSpatial.class, spatial.getId());
        assertThat(loaded.getCartesianPoint3d()).isEqualTo(point);
    }

    @Test
    public void convertPersistAndLoadGeographicPoint2dForRelationship() {
        Session session = sessionFactory.openSession();
        SomethingRelationship somethingRelated = new SomethingRelationship();
        GeographicPoint2d point = new GeographicPoint2d(1, 2);
        somethingRelated.setGeographicPoint2d(point);
        session.save(somethingRelated);

        session.clear();
        SomethingRelationship loaded = session.load(SomethingRelationship.class, somethingRelated.getId());
        assertThat(loaded.getGeographicPoint2d()).isEqualTo(point);
    }

    @Test
    public void convertPersistAndLoadGeographicPoint3dForRelationship() {
        Session session = sessionFactory.openSession();
        SomethingRelationship somethingRelated = new SomethingRelationship();
        GeographicPoint3d point = new GeographicPoint3d(1, 2, 3);
        somethingRelated.setGeographicPoint3d(point);
        session.save(somethingRelated);

        session.clear();
        SomethingRelationship loaded = session.load(SomethingRelationship.class, somethingRelated.getId());
        assertThat(loaded.getGeographicPoint3d()).isEqualTo(point);
    }

    @Test
    public void convertPersistAndLoadCartesianPoint2dForRelationship() {
        Session session = sessionFactory.openSession();
        SomethingRelationship somethingRelated = new SomethingRelationship();
        CartesianPoint2d point = new CartesianPoint2d(1, 2);
        somethingRelated.setCartesianPoint2d(point);
        session.save(somethingRelated);

        session.clear();
        SomethingRelationship loaded = session.load(SomethingRelationship.class, somethingRelated.getId());
        assertThat(loaded.getCartesianPoint2d()).isEqualTo(point);
    }

    @Test
    public void convertPersistAndLoadCartesianPoint3dForRelationship() {
        Session session = sessionFactory.openSession();
        SomethingRelationship somethingRelated = new SomethingRelationship();
        CartesianPoint3d point = new CartesianPoint3d(1, 2, 3);
        somethingRelated.setCartesianPoint3d(point);
        session.save(somethingRelated);

        session.clear();
        SomethingRelationship loaded = session.load(SomethingRelationship.class, somethingRelated.getId());
        assertThat(loaded.getCartesianPoint3d()).isEqualTo(point);
    }

}
