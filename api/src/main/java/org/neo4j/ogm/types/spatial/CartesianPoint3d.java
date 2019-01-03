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
