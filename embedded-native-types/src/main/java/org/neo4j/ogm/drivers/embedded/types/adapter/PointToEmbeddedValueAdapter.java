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
