package org.neo4j.spring.integration.movies.domain;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label = "")
public abstract class Entity {

    Long id;
}
