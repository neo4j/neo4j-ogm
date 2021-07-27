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

import java.util.List;
import java.util.function.Function;

import org.neo4j.ogm.types.spatial.AbstractPoint;
import org.neo4j.ogm.types.spatial.Coordinate;
import org.neo4j.ogm.types.spatial.PointBuilder;
import org.neo4j.values.storable.PointValue;

/**
 * @author Michael J. Simons
 */
public class EmbeddedValueToPointAdapter implements Function<PointValue, AbstractPoint> {

    @Override
    public AbstractPoint apply(PointValue point) {

        List<Double> nativeCoordinate = point.getCoordinate().getCoordinate();
        Coordinate coordinate;
        if (nativeCoordinate.size() == 2) {
            coordinate = new Coordinate(nativeCoordinate.get(0), nativeCoordinate.get(1));
        } else if (nativeCoordinate.size() == 3) {
            coordinate = new Coordinate(nativeCoordinate.get(0), nativeCoordinate.get(1), nativeCoordinate.get(2));
        } else {
            throw new IllegalArgumentException("Invalid number of coordinate fields: " + nativeCoordinate.size());
        }

        return PointBuilder.withSrid(point.getCRS().getCode()).build(coordinate);
    }
}
