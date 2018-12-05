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

package org.neo4j.ogm.cypher.function;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.cypher.Filter;

/**
 * @author Jasper Blues
 */
public class DistanceComparison implements FilterFunction<DistanceFromPoint> {

    private static final String LATITUDE_PROPERTY_SUFFIX = ".latitude";
    private static final String LONGITUDE_PROPERTY_SUFFIX = ".longitude";
    private DistanceFromPoint value;
    private Filter filter;

    public DistanceComparison(DistanceFromPoint value) {
        this.value = value;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public DistanceFromPoint getValue() {
        return value;
    }

    @Override
    public String expression(String nodeIdentifier) {
        String latitude = nodeIdentifier + LATITUDE_PROPERTY_SUFFIX;
        String longitude = nodeIdentifier + LONGITUDE_PROPERTY_SUFFIX;

        return String.format("distance(point({latitude: %s, longitude: %s}),point({latitude:{lat}, longitude:{lon}})) " +
            "%s {distance} ", latitude, longitude, filter.getComparisonOperator().getValue());
    }

    @Override
    public Map<String, Object> parameters() {

        Map<String, Object> map = new HashMap<>();
        map.put("lat", value.getLatitude());
        map.put("lon", value.getLongitude());
        map.put("distance", value.getDistance());
        return map;
    }
}
