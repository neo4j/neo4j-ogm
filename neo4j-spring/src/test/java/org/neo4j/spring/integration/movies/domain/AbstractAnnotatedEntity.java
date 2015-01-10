package org.neo4j.spring.integration.movies.domain;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public abstract class AbstractAnnotatedEntity {

    @GraphId
    Long nodeId;
}
