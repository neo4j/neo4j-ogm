package org.neo4j.ogm.example;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Person {

    @Id
    String name;

    @Override
    public String toString() {
        return name;
    }
}
