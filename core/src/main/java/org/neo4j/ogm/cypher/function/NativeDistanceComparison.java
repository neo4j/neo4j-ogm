/*
 * Copyright (c) 2002-2019 "Neo4j,"
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

import org.neo4j.ogm.cypher.Filter;

public class NativeDistanceComparison implements FilterFunction<DistanceFromNativePoint> {

    private static final String DISTANCE_VALUE_PARAMETER = "distanceValue";
    private static final String OGM_POINT_PARAMETER = "ogmPoint";
    private final DistanceFromNativePoint distanceFromNativePoint;
    private Filter filter;

    private NativeDistanceComparison(DistanceFromNativePoint distanceFromNativePoint) {
        this.distanceFromNativePoint = distanceFromNativePoint;
    }

    public static NativeDistanceComparison distanceComparisonFor(DistanceFromNativePoint distanceFromNativePoint) {
        return new NativeDistanceComparison(distanceFromNativePoint);
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
    public DistanceFromNativePoint getValue() {
        return distanceFromNativePoint;
    }

    @Override
    public String expression(String nodeIdentifier) {

        String pointPropertyOfEntity = nodeIdentifier + "." + getFilter().getPropertyName();
        String comparisonOperator = getFilter().getComparisonOperator().getValue();

        return String.format(
            "distance($%s,%s) %s $%s ",
            OGM_POINT_PARAMETER, pointPropertyOfEntity, comparisonOperator, DISTANCE_VALUE_PARAMETER);
    }

    @Override
    public Map<String, Object> parameters() {

        Map<String, Object> map = new HashMap<>();

        map.put(OGM_POINT_PARAMETER, distanceFromNativePoint.getPoint());
        map.put(DISTANCE_VALUE_PARAMETER, distanceFromNativePoint.getDistance());
        return map;

    }
}
