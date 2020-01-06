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

import java.util.Objects;

/**
 * @author Michael J. Simons
 */
public class Coordinate {
    private final double x;

    private final double y;

    private final Double z;

    public Coordinate(double x, double y) {
        this(x, y, null);
    }

    public Coordinate(double x, double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Double getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Coordinate)) {
            return false;
        }
        Coordinate that = (Coordinate) o;
        return Double.compare(that.x, x) == 0 &&
            Double.compare(that.y, y) == 0 &&
            Objects.equals(z, that.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
