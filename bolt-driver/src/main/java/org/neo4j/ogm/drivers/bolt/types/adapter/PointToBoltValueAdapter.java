/*
 * Copyright (c) 2002-2022 "Neo4j,"
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

import java.util.function.Function;

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Point;
import org.neo4j.ogm.types.spatial.AbstractPoint;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;

/**
 * @author Michael J. Simons
 */
public class PointToBoltValueAdapter implements Function<AbstractPoint, Point> {

    @Override
    public Point apply(AbstractPoint object) {

        Value value;
        if (object instanceof CartesianPoint2d) {
            CartesianPoint2d point = (CartesianPoint2d) object;
            value = Values.point(point.getSrid(), point.getX(), point.getY());
        } else if (object instanceof CartesianPoint3d) {
            CartesianPoint3d point = (CartesianPoint3d) object;
            value = Values.point(point.getSrid(), point.getX(), point.getY(), point.getZ());
        } else if (object instanceof GeographicPoint2d) {
            GeographicPoint2d point = (GeographicPoint2d) object;
            value = Values.point(point.getSrid(), point.getLongitude(), point.getLatitude());
        } else if (object instanceof GeographicPoint3d) {
            GeographicPoint3d point = (GeographicPoint3d) object;
            value = Values.point(point.getSrid(), point.getLongitude(), point.getLatitude(),
                point.getHeight());
        } else {
            throw new IllegalArgumentException("Unsupported point implementation: " + object.getClass());
        }

        return value.asPoint();
    }
}
