package org.neo4j.ogm.integration.hierarchy.domain.annotated;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label = "Child")
public class AnnotatedNamedChildWithAnnotatedAbstractNamedParent extends AnnotatedAbstractNamedParent {
}
