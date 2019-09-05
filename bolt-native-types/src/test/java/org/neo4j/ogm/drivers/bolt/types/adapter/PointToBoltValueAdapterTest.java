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
package org.neo4j.ogm.drivers.bolt.types.adapter;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.neo4j.driver.types.Point;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;

/**
 * @author Michael J. Simons
 */
public class PointToBoltValueAdapterTest {
    @Test
    public void mappingShouldWork() {
        PointToBoltValueAdapter adapter = new PointToBoltValueAdapter();

        Point point;

        point = adapter.apply(new GeographicPoint2d(10, 20));
        Assertions.assertThat(point.srid()).isEqualTo(4326);
        Assertions.assertThat(point.x()).isEqualTo(20.0);
        Assertions.assertThat(point.y()).isEqualTo(10.0);

        point = adapter.apply(new CartesianPoint2d(10, 20));
        Assertions.assertThat(point.srid()).isEqualTo(7203);
        Assertions.assertThat(point.x()).isEqualTo(10.0);
        Assertions.assertThat(point.y()).isEqualTo(20.0);

        point = adapter.apply(new GeographicPoint3d(10.0, 20.0, 30));
        Assertions.assertThat(point.srid()).isEqualTo(4979);
        Assertions.assertThat(point.x()).isEqualTo(20.0);
        Assertions.assertThat(point.y()).isEqualTo(10.0);
        Assertions.assertThat(point.z()).isEqualTo(30.0);

        point = adapter.apply(new CartesianPoint3d(10.0, 20.0, 30));
        Assertions.assertThat(point.srid()).isEqualTo(9157);
        Assertions.assertThat(point.x()).isEqualTo(10.0);
        Assertions.assertThat(point.y()).isEqualTo(20.0);
        Assertions.assertThat(point.z()).isEqualTo(30.0);
    }
}
