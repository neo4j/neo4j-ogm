/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
package org.neo4j.ogm.types.spatial;

/**
 * @author Michael J. Simons
 */
public final class PointBuilder {

    private final int srid;

    public static PointBuilder withSrid(int srid) {
        return new PointBuilder(srid);
    }

    private PointBuilder(int srid) {
        this.srid = srid;
    }

    public AbstractPoint build(Coordinate coordinate) {

        boolean is3d = coordinate.getZ() != null;

        if (srid == CartesianPoint2d.SRID || srid == CartesianPoint3d.SRID) {
            return is3d ? new CartesianPoint3d(coordinate) : new CartesianPoint2d(coordinate);
        } else {
            return is3d ? new GeographicPoint3d(coordinate, srid) : new GeographicPoint2d(coordinate, srid);
        }
    }
}
