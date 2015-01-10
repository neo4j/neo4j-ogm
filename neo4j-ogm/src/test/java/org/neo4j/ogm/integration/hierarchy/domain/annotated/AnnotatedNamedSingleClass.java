package org.neo4j.ogm.integration.hierarchy.domain.annotated;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label = "Single")
public class AnnotatedNamedSingleClass {

    @GraphId
    private Long nodeId;
}
