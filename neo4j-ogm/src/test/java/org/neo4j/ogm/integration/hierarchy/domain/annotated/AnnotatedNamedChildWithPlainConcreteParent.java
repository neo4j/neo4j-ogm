package org.neo4j.ogm.integration.hierarchy.domain.annotated;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.integration.hierarchy.domain.plain.PlainConcreteParent;

@NodeEntity(label = "Child")
public class AnnotatedNamedChildWithPlainConcreteParent extends PlainConcreteParent {
}
