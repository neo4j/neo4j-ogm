/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.cypher.function;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.PropertyValueTransformer;

/**
 * @author Jasper Blues
 * @author Michael J. Simons
 */
public class DistanceComparison implements FilterFunction<DistanceFromPoint> {

    private static final String LATITUDE_PROPERTY_SUFFIX = ".latitude";
    private static final String LONGITUDE_PROPERTY_SUFFIX = ".longitude";
    protected final ComparisonOperator operator;
    protected final DistanceFromPoint value;

    public DistanceComparison(DistanceFromPoint value) {
        this(ComparisonOperator.LESS_THAN, value);
    }

    public DistanceComparison(ComparisonOperator operator, DistanceFromPoint value) {
        this.operator = operator;
        this.value = value;
    }

    public DistanceComparison withOperator(ComparisonOperator newOperator) {
        return this.operator == newOperator ? this : new DistanceComparison(newOperator, value);
    }

    @Override
    public DistanceFromPoint getValue() {
        return value;
    }

    @Override
    public String expression(String nodeIdentifier, String filteredProperty,
        UnaryOperator<String> createUniqueParameterName) {

        String latitude = nodeIdentifier + LATITUDE_PROPERTY_SUFFIX;
        String longitude = nodeIdentifier + LONGITUDE_PROPERTY_SUFFIX;
        return String
            .format("point.distance(point({latitude: %s, longitude: %s}),point({latitude: $lat, longitude: $lon})) " +
                "%s $distance ", latitude, longitude, operator.getValue());
    }

    @Override
    public Map<String, Object> parameters(UnaryOperator<String> createUniqueParameterName,
        PropertyValueTransformer valueTransformer) {

        Map<String, Object> map = new HashMap<>();
        map.put("lat", value.getLatitude());
        map.put("lon", value.getLongitude());
        map.put("distance", value.getDistance());
        return map;
    }
}
