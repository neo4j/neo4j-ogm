package org.neo4j.ogm.domain.cineasts.annotated;

import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity
public class Role {
    Movie movie;
    Actor actor;
    String role;
}
