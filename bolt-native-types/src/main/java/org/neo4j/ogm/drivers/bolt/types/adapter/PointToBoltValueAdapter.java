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

import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.Point;
import org.neo4j.ogm.types.spatial.AbstractPoint;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.Coordinate;
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
