package org.neo4j.ogm.persistence.types.nativetypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class SomethingRelated {

    LocalDate localDate;
    private Long id;

    @Relationship(value = "REL", direction = Relationship.INCOMING)
    private Collection<SomethingRelationship> rels = new ArrayList<>();

    public Collection<SomethingRelationship> getRels() {
        return rels;
    }
}
