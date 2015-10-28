package org.neo4j.ogm.model;

import java.util.List;

/**
 * @author vince
 */
public interface Node {
    String[] getLabels();

    Long getId();

    List<Property<String, Object>> getPropertyList();
}
