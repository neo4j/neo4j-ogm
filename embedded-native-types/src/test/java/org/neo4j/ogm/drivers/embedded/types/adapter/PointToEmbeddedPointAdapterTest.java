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
import org.neo4j.graphdb.spatial.Point;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;

/**
 * @author Michael J. Simons
 */
public class PointToEmbeddedPointAdapterTest {
    @Test
    public void mappingShouldWork() {
        PointToEmbeddedPointAdapter adapter = new PointToEmbeddedPointAdapter();

        Point point;

        point = adapter.apply(new GeographicPoint2d(10, 20));
        Assertions.assertThat(point.getCRS().getCode()).isEqualTo(4326);
        Assertions.assertThat(point.getCoordinate().getCoordinate().get(0)).isEqualTo(20.0);
        Assertions.assertThat(point.getCoordinate().getCoordinate().get(1)).isEqualTo(10.0);

        point = adapter.apply(new CartesianPoint2d(10, 20));
        Assertions.assertThat(point.getCRS().getCode()).isEqualTo(7203);
        Assertions.assertThat(point.getCoordinate().getCoordinate().get(0)).isEqualTo(10.0);
        Assertions.assertThat(point.getCoordinate().getCoordinate().get(1)).isEqualTo(20.0);

        point = adapter.apply(new GeographicPoint3d(10.0, 20.0, 30));
        Assertions.assertThat(point.getCRS().getCode()).isEqualTo(4979);
        Assertions.assertThat(point.getCoordinate().getCoordinate().get(0)).isEqualTo(20.0);
        Assertions.assertThat(point.getCoordinate().getCoordinate().get(1)).isEqualTo(10.0);
        Assertions.assertThat(point.getCoordinate().getCoordinate().get(2)).isEqualTo(30.0);

        point = adapter.apply(new CartesianPoint3d(10.0, 20.0, 30));
        Assertions.assertThat(point.getCRS().getCode()).isEqualTo(9157);
        Assertions.assertThat(point.getCoordinate().getCoordinate().get(0)).isEqualTo(10.0);
        Assertions.assertThat(point.getCoordinate().getCoordinate().get(1)).isEqualTo(20.0);
        Assertions.assertThat(point.getCoordinate().getCoordinate().get(2)).isEqualTo(30.0);
    }
}
