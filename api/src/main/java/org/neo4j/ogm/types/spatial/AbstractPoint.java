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
 * Not part of public API, subject to change without notice.
 * 
 * @author Michael J. Simons
 */
public abstract class AbstractPoint {

    protected final Coordinate coordinate;

    private final Integer srid;

    AbstractPoint(Coordinate coordinate, Integer srid) {
        this.coordinate = coordinate;
        this.srid = srid;
    }

    public final Coordinate getCoordinate() {
        return coordinate;
    }

    public final Integer getSrid() {
        return srid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AbstractPoint))
            return false;
        AbstractPoint that = (AbstractPoint) o;
        return Objects.equals(coordinate, that.coordinate) &&
            Objects.equals(srid, that.srid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinate, srid);
    }
}
