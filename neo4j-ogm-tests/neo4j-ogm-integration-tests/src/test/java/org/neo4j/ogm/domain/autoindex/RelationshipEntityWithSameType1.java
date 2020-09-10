package org.neo4j.ogm.domain.autoindex;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * @author Gerrit Meier
 */
@RelationshipEntity(type = "SAME_TYPE")
public class RelationshipEntityWithSameType1 {

    @Id
    private Long id;
}
