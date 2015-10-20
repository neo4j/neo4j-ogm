package org.neo4j.ogm.api.model;

import java.util.List;

/**
 * @author vince
 */
public interface Edge {
    String getType();

    Long getStartNode();

    Long getEndNode();

    Long getId();

    List<Property<String, Object>> getPropertyList();
}
