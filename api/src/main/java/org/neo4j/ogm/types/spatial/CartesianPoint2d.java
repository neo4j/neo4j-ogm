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
public class CartesianPoint2d extends AbstractPoint {

    static final int SRID = 7203;

    CartesianPoint2d(Coordinate coordinate) {
        super(coordinate, SRID);
    }

    public CartesianPoint2d(double x, double y) {
        super(new Coordinate(x, y), SRID);
    }

    public double getX() {
        return coordinate.getX();
    }

    public double getY() {
        return coordinate.getY();
    }

    @Override public String toString() {
        return "CartesianPoint2d{" +
            "x=" + getX() +
            ", y=" + getY() +
            '}';
    }
}
