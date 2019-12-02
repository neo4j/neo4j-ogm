package org.neo4j.ogm.domain.gh552;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity("Thing")
public class ThingEntity {
    @Id @GeneratedValue
    private Long id;

    private String name;
}

