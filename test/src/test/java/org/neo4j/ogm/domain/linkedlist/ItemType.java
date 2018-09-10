package org.neo4j.ogm.domain.linkedlist;

import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author <a href="mailto:atul.mahind@kiwigrid.com">Atul Mahind</a>
 */
@NodeEntity
public class ItemType {

    private Long id;

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }
}
