package org.neo4j.ogm.domain.nodes;

import java.util.List;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class DataItem extends BaseEntity {

    private String nodeId;

    /*
     * This field is here, b/c the neo4j ogm driver optimizes queries against the class given to the query.
     * If we want the returned child entities to have its releations mapped as well, we need to tell OGM all
     * the fields by adding them here
     */
    @Relationship(type = "USES")
    protected List<Variable> variables;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
