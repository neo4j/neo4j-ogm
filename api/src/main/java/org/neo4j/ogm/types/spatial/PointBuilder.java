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
package org.neo4j.ogm.types.spatial;

/**
 * @author Michael J. Simons
 */
public final class PointBuilder {

    private final int srid;

    private Coordinate coordinate;

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
