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
public class GeographicPoint3d extends AbstractPoint {

    GeographicPoint3d(Coordinate coordinate, Integer srid) {
        super(coordinate, srid);
    }

    public GeographicPoint3d(double latitude, double longitude, double elevation) {
        super(new Coordinate(longitude, latitude, elevation), 4979);
    }

    public double getLongitude() {
        return coordinate.getX();
    }

    public double getLatitude() {
        return coordinate.getY();
    }

    public double getHeight() {
        return coordinate.getZ();
    }

    @Override
    public String toString() {
        return "GeographicPoint3d{" +
            "longitude=" + getLongitude() +
            ", latitude=" + getLatitude() +
            ", height=" + getHeight() +
            ", srid=" + getSrid() +
            '}';
    }
}
