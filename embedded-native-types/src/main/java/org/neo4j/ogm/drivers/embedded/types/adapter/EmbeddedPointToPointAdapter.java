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

import java.util.List;
import java.util.function.Function;

import org.neo4j.ogm.types.spatial.PointBuilder;
import org.neo4j.values.storable.PointValue;
import org.neo4j.ogm.types.spatial.AbstractPoint;
import org.neo4j.ogm.types.spatial.Coordinate;

/**
 * @author Michael J. Simons
 */
public class EmbeddedPointToPointAdapter implements Function<PointValue, AbstractPoint> {

    @Override
    public AbstractPoint apply(PointValue point) {

        if (point == null) {
            return null;
        }

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
