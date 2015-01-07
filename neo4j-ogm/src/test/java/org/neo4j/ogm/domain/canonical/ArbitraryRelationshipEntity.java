package org.neo4j.ogm.domain.canonical;

import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * NB: this isn't actually used to save anything at the time of writing; it's just so we can test meta-data resolution for
 * relationship entities.
 */
@RelationshipEntity(type = "MEMBER_OF")
public class ArbitraryRelationshipEntity {

    // nothing here yet

}
