/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */
package org.neo4j.ogm.drivers.bolt.types.adapter;

import java.util.function.Function;

import org.neo4j.driver.v1.types.Point;
import org.neo4j.ogm.types.spatial.AbstractPoint;
import org.neo4j.ogm.types.spatial.Coordinate;
import org.neo4j.ogm.types.spatial.PointBuilder;

/**
 * @author Michael J. Simons
 */
public class BoltPointToPointAdapter implements Function<Point, AbstractPoint> {

    @Override
    public AbstractPoint apply(Point point) {

        if (point == null) {
            return null;
        }

        Coordinate coordinate = new Coordinate(point.x(), point.y(), Double.isNaN(point.z()) ? null : point.z());
        return PointBuilder.withSrid(point.srid()).build(coordinate);
    }
}
