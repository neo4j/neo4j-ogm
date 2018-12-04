package org.neo4j.ogm.cypher.function;

import org.neo4j.ogm.types.spatial.AbstractPoint;

public class DistanceFromSpatialPoint {

    private final AbstractPoint point;
    private final double distance;

    public DistanceFromSpatialPoint(AbstractPoint point, double distance) {

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
