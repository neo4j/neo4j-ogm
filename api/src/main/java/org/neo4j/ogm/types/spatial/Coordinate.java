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

import java.util.Objects;

/**
 * @author Michael J. Simons
 */
public class Coordinate {
    private final double x;

    private final double y;

    private final Double z;

    public Coordinate(double x, double y) {
        this(x, y, null);
    }

    public Coordinate(double x, double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Double getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Coordinate))
            return false;
        Coordinate that = (Coordinate) o;
        return Double.compare(that.x, x) == 0 &&
            Double.compare(that.y, y) == 0 &&
            Objects.equals(z, that.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
