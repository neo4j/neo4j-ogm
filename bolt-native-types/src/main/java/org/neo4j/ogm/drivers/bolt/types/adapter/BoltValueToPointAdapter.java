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

import org.neo4j.driver.types.Point;
import org.neo4j.ogm.types.spatial.AbstractPoint;
import org.neo4j.ogm.types.spatial.Coordinate;
import org.neo4j.ogm.types.spatial.PointBuilder;

/**
 * @author Michael J. Simons
 */
public class BoltValueToPointAdapter implements Function<Point, AbstractPoint> {

    @Override
    public AbstractPoint apply(Point point) {

        Coordinate coordinate = new Coordinate(point.x(), point.y(), Double.isNaN(point.z()) ? null : point.z());
        return PointBuilder.withSrid(point.srid()).build(coordinate);
    }
}
