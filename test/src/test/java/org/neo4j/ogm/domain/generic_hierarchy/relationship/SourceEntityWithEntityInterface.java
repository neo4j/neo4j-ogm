package org.neo4j.ogm.domain.generic_hierarchy.relationship;

import org.neo4j.ogm.annotation.Relationship;

public class SourceEntityWithEntityInterface implements EntityInterface {
    private Long id;
    @Relationship(type = "GENERIC")
    public GenericRelationship<SourceEntityWithEntityInterface, ? extends EntitySuperInterface> relationship;
}
