package org.neo4j.ogm.domain.lazyloading;

import java.util.Set;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Andreas Berger
 */
@NodeEntity
public class Node extends BaseNodeEntity {

    @JsonIgnore
    @Relationship(type = "CHILD_OF", direction = Relationship.INCOMING)
    private Set<BaseNodeEntity> childNodes;

    public Set<BaseNodeEntity> getChildNodes() {
        read("childNodes");
        return childNodes;
    }

    public Node setChildNodes(Set<BaseNodeEntity> childNodes) {
        write("childNodes", childNodes);
        this.childNodes = childNodes;
        return this;
    }
}
