/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */
package org.neo4j.ogm.drivers.embedded.types.adapter;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.neo4j.ogm.types.spatial.AbstractPoint;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;
import org.neo4j.values.storable.CoordinateReferenceSystem;
import org.neo4j.values.storable.Values;

/**
 * @author Michael J. Simons
 */
public class EmbeddedValueToPointAdapterTest {
    @Test
    public void mappingShouldWork() {
        EmbeddedValueToPointAdapter adapter = new EmbeddedValueToPointAdapter();

        AbstractPoint point;

        point = adapter.apply(Values.pointValue(CoordinateReferenceSystem.WGS84, 10.0, 20.0));
        Assertions.assertThat(point).isInstanceOf(GeographicPoint2d.class);
        Assertions.assertThat(point.getSrid()).isEqualTo(4326);
        Assertions.assertThat(((GeographicPoint2d) point).getLatitude()).isEqualTo(20.0);
        Assertions.assertThat(((GeographicPoint2d) point).getLongitude()).isEqualTo(10.0);

        point = adapter.apply(Values.pointValue(CoordinateReferenceSystem.Cartesian, 10.0, 20.0));
        Assertions.assertThat(point).isInstanceOf(CartesianPoint2d.class);
        Assertions.assertThat(point.getSrid()).isEqualTo(7203);
        Assertions.assertThat(((CartesianPoint2d) point).getX()).isEqualTo(10.0);
        Assertions.assertThat(((CartesianPoint2d) point).getY()).isEqualTo(20.0);

        point = adapter.apply(Values.pointValue(CoordinateReferenceSystem.WGS84_3D, 10.0, 20.0, 30));
        Assertions.assertThat(point).isInstanceOf(GeographicPoint3d.class);
        Assertions.assertThat(point.getSrid()).isEqualTo(4979);
        Assertions.assertThat(((GeographicPoint3d) point).getLatitude()).isEqualTo(20.0);
        Assertions.assertThat(((GeographicPoint3d) point).getLongitude()).isEqualTo(10.0);
        Assertions.assertThat(((GeographicPoint3d) point).getElevation()).isEqualTo(30.0);

        point = adapter.apply(Values.pointValue(CoordinateReferenceSystem.Cartesian_3D, 10.0, 20.0, 30));
        Assertions.assertThat(point).isInstanceOf(CartesianPoint3d.class);
        Assertions.assertThat(point.getSrid()).isEqualTo(9157);
        Assertions.assertThat(((CartesianPoint3d) point).getX()).isEqualTo(10.0);
        Assertions.assertThat(((CartesianPoint3d) point).getY()).isEqualTo(20.0);
        Assertions.assertThat(((CartesianPoint3d) point).getZ()).isEqualTo(30.0);

    }

}
