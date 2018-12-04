package org.neo4j.ogm.cypher.function;

import java.util.HashMap;
import java.util.Map;

public class SpatialDistanceComparison extends DistanceComparison {

    private final DistanceFromSpatialPoint distanceFromSpatialPoint;

    private SpatialDistanceComparison(DistanceFromSpatialPoint distanceFromSpatialPoint) {
        super(null);
        this.distanceFromSpatialPoint = distanceFromSpatialPoint;
    }

    public static DistanceComparison distanceComparisonFor(DistanceFromSpatialPoint distanceFromSpatialPoint) {
        return new SpatialDistanceComparison(distanceFromSpatialPoint);
    }

    @Override
    public String expression(String nodeIdentifier) {
        String pointPropertyOfEntity = nodeIdentifier + "." + getFilter().getPropertyName();

        return String.format("distance({ogmPoint},%s) " +
            "%s {distance} ", pointPropertyOfEntity, getFilter().getComparisonOperator().getValue());
    }

    @Override public Map<String, Object> parameters() {
        Map<String, Object> map = new HashMap<>();

        map.put("ogmPoint", distanceFromSpatialPoint.getPoint());
        map.put("distance", distanceFromSpatialPoint.getDistance());
        return map;

    }
}
