package org.neo4j.ogm.example;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import java.util.List;

@RelationshipEntity(type = "ACTED_IN")
public class Actor {

    @Id @GeneratedValue
    Long id;

    @StartNode
    Person person;

    @EndNode
    Movie movie;

    List<String> roles;

    @Override public String toString() {
        return "%s as %s".formatted(person.name, roles);
    }
}
