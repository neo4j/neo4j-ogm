package org.neo4j.ogm.domain.forum;

import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * NB: this isn't actually in use at the time of writing, it's just so we can test meta-data resolution for relationship
 * entities.
 */
@RelationshipEntity(type = "MEMBER_OF")
public class ArbitraryRelationshipEntity {

    // nothing here yet

}
