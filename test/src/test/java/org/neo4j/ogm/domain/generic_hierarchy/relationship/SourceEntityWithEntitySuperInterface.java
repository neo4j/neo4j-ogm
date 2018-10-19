package org.neo4j.ogm.domain.generic_hierarchy.relationship;

import org.neo4j.ogm.annotation.Relationship;

public class SourceEntityWithEntitySuperInterface implements EntitySuperInterface {
    private Long id;

    @Relationship(type = "GENERIC")
    public GenericRelationship<SourceEntityWithEntitySuperInterface, ? extends EntitySuperInterface> relationship;
}
