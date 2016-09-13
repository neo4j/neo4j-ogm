/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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


import org.neo4j.ogm.cypher.Filter;

import java.util.HashMap;
import java.util.Map;

public class DistanceComparison implements FilterFunction<DistanceFromPoint> {

    private DistanceFromPoint value;

    public DistanceComparison(DistanceFromPoint value) {
        this.value = value;
    }

    @Override
    public String cypherFragment(Filter filter, String nodeIdentifier) {
        return String.format("distance(point(%s),point({latitude:{lat}, longitude:{lon}})) " +
                "%s {distance} ", nodeIdentifier, filter.getComparisonOperator().getValue());
    }

    @Override
    public DistanceFromPoint getValue() {
        return value;
    }

    @Override
    public void setValue(DistanceFromPoint value) {
        this.value = value;
    }

    @Override
    public Map<String, Object> cypherProperties(Filter filter) {
        Map<String, Object> map = new HashMap<>();
        map.put("lat", value.getLatitude());
        map.put("lon", value.getLongitude());
        map.put("distance", value.getDistance());
        return map;
    }
}
