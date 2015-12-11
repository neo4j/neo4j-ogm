package org.neo4j.ogm.domain.mappings;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Nils Dr\u00F6ge
 */
@RelationshipEntity(type = "RELATED_TO")
public class RichRelation {
    @StartNode
    public Person person;
    @EndNode
    public Article article;
    Long id;

    public RichRelation() {
    }
}
