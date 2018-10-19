package org.neo4j.ogm.domain.generic_hierarchy.relationship;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = "GENERIC")
public class GenericRelationship<S extends EntitySuperInterface, T extends EntitySuperInterface> {

    private Long id;

    @StartNode
    public S source;

    @EndNode
    public T target;
}
