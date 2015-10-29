package org.neo4j.ogm.model;

import java.util.Set;

/**
 * @author vince
 */
public interface GraphModel {
    Set<Node> getNodes();

    Set<Edge> getRelationships();
}
