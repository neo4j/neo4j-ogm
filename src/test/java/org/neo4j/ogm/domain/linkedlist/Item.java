package org.neo4j.ogm.domain.linkedlist;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Vince Bickers
 */
public class Item {

    private Long id;

    @Relationship(type = "NEXT", direction=Relationship.OUTGOING)
    public Item next;

    @Relationship(type = "NEXT", direction=Relationship.INCOMING)
    public Item previous;

    public Long getId() {
        return id;
    }
}
