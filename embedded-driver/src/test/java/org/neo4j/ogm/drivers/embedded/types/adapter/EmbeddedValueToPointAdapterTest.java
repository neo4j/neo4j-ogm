/*
 * Copyright (c) 2002-2021 "Neo4j,"
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
        Assertions.assertThat(((GeographicPoint3d) point).getHeight()).isEqualTo(30.0);

        point = adapter.apply(Values.pointValue(CoordinateReferenceSystem.Cartesian_3D, 10.0, 20.0, 30));
        Assertions.assertThat(point).isInstanceOf(CartesianPoint3d.class);
        Assertions.assertThat(point.getSrid()).isEqualTo(9157);
        Assertions.assertThat(((CartesianPoint3d) point).getX()).isEqualTo(10.0);
        Assertions.assertThat(((CartesianPoint3d) point).getY()).isEqualTo(20.0);
        Assertions.assertThat(((CartesianPoint3d) point).getZ()).isEqualTo(30.0);

    }

}
