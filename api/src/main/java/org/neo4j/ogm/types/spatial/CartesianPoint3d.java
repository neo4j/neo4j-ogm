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
public class CartesianPoint3d extends AbstractPoint {

    static final int SRID = 9157;

    CartesianPoint3d(Coordinate coordinate) {
        super(coordinate, SRID);
    }

    public CartesianPoint3d(double x, double y, double z) {
        super(new Coordinate(x, y, z), SRID);
    }

    public double getX() {
        return coordinate.getX();
    }

    public double getY() {
        return coordinate.getY();
    }

    public Double getZ() {
        return coordinate.getZ();
    }

    @Override
    public String toString() {
        return "CartesianPoint3d{" +
            "x=" + getX() +
            ", y=" + getY() +
            ", z=" + getZ() +
            '}';
    }
}
