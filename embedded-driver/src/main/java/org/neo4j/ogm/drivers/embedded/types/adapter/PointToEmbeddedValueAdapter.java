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

import java.util.function.Function;

import org.neo4j.ogm.types.spatial.AbstractPoint;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;
import org.neo4j.values.storable.CoordinateReferenceSystem;
import org.neo4j.values.storable.PointValue;
import org.neo4j.values.storable.Values;

/**
 * @author Michael J. Simons
 */
public class PointToEmbeddedValueAdapter implements Function<AbstractPoint, PointValue> {

    @Override
    public PointValue apply(AbstractPoint object) {

        if (object instanceof CartesianPoint2d) {
            CartesianPoint2d point = (CartesianPoint2d) object;
            return Values.pointValue(CoordinateReferenceSystem.Cartesian, point.getX(), point.getY());
        } else if (object instanceof CartesianPoint3d) {
            CartesianPoint3d point = (CartesianPoint3d) object;
            return Values.pointValue(CoordinateReferenceSystem.Cartesian_3D, point.getX(), point.getY(),
                point.getZ());
        } else if (object instanceof GeographicPoint2d) {
            GeographicPoint2d point = (GeographicPoint2d) object;
            return Values.pointValue(CoordinateReferenceSystem.WGS84, point.getLongitude(), point.getLatitude());
        } else if (object instanceof GeographicPoint3d) {
            GeographicPoint3d point = (GeographicPoint3d) object;
            return Values.pointValue(CoordinateReferenceSystem.WGS84_3D, point.getLongitude(), point.getLatitude(),
                point.getHeight());
        } else {
            throw new IllegalArgumentException("Unsupported point implementation: " + object.getClass());
        }
    }
}
