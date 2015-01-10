package org.neo4j.ogm.integration.hierarchy.domain.annotated;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label = "Parent")
public abstract class AnnotatedAbstractNamedParent {

    private Long id;
}
