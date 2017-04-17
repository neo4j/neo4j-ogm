package org.neo4j.ogm.domain.temporalNetwork;

import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity(type = "ShopState")
public class ShopTimeRelation extends AbstractTimeRelation<ShopIN, ShopSN>  {
    // empty by design
}
