package org.neo4j.ogm.domain.nested;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class NestingClass {

    @NodeEntity
    public static class Something {
        @Id
        @GeneratedValue
        private Long id;

        private String name;

    }
}
