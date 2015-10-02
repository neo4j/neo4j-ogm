package org.neo4j.ogm.api.model;

import java.util.Map;

/**
 * @author vince
 */
public interface Edge {
    String getType();

    Long getStartNode();

    Long getEndNode();

    Long getId();

    Map<String, Object> getProperties();
}
