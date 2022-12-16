package org.neo4j.ogm.example;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Gerrit Meier
 */
@RelationshipEntity("REVIEWED")
public class Reviewer {

    @Id @GeneratedValue
    Long id;

    @Property("summary")
    String review;

    int rating;

    @EndNode
    Movie movie;

    @StartNode
    Person person;

    @Override
    public String toString() {
        return "%s gave a rating of %d with review: '%s'".formatted(person.name, rating, review);
    }
}
