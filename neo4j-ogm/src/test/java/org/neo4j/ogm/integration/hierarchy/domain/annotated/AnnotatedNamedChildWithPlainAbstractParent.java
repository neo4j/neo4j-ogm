package org.neo4j.ogm.integration.hierarchy.domain.annotated;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.integration.hierarchy.domain.plain.PlainAbstractParent;

@NodeEntity(label = "Child")
public class AnnotatedNamedChildWithPlainAbstractParent extends PlainAbstractParent {
}
