package org.neo4j.ogm.cypher.function;

import org.neo4j.ogm.types.spatial.AbstractPoint;

public class DistanceFromNativePoint {

    private final AbstractPoint point;
    private final double distance;

    public DistanceFromNativePoint(AbstractPoint point, double distance) {

        this.point = point;
        this.distance = distance;
    }

    public AbstractPoint getPoint() {
        return point;
    }

    public double getDistance() {
        return distance;
    }
}
