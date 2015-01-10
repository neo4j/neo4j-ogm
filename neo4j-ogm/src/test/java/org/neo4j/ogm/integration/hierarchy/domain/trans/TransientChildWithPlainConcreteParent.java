package org.neo4j.ogm.integration.hierarchy.domain.trans;

import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.integration.hierarchy.domain.plain.PlainConcreteParent;

@Transient
public class TransientChildWithPlainConcreteParent extends PlainConcreteParent {
}
