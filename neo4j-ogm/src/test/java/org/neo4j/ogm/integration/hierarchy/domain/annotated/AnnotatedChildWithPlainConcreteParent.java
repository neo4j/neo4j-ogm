package org.neo4j.ogm.integration.hierarchy.domain.annotated;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.integration.hierarchy.domain.plain.PlainConcreteParent;

@NodeEntity
public class AnnotatedChildWithPlainConcreteParent extends PlainConcreteParent {
}
