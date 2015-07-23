package org.neo4j.ogm.domain.mappings;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Nils Dröge
 */
@RelationshipEntity(type = "RELATED_TO")
public class RichRelation
{
    @StartNode
    public Person person;

    @EndNode
    public Article article;
}
