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
package org.neo4j.ogm.types.spatial;

import java.util.Objects;

/**
 * Not part of public API, subject to change without notice.
 *
 * @author Michael J. Simons
 */
public abstract class AbstractPoint {

    protected final Coordinate coordinate;

    private final Integer srid;

    AbstractPoint(Coordinate coordinate, Integer srid) {
        this.coordinate = coordinate;
        this.srid = srid;
    }

    final Coordinate getCoordinate() {
        return coordinate;
    }

    public final Integer getSrid() {
        return srid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractPoint)) {
            return false;
        }
        AbstractPoint that = (AbstractPoint) o;
        return Objects.equals(coordinate, that.coordinate) &&
            Objects.equals(srid, that.srid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinate, srid);
    }
}
