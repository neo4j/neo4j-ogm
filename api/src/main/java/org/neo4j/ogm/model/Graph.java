package org.neo4j.ogm.model;

import java.util.Set;

/**
 * @author vince
 */
public interface Graph {
    Set<Node> getNodes();

    Set<Edge> getRelationships();
}
