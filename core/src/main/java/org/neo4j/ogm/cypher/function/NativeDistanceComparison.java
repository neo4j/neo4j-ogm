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
            "distance({%s},%s) %s {%s} ",
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
