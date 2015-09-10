package org.neo4j.ogm.domain.temporalNetwork;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label = "ShopSN")
public class ShopSN extends AbstractStateNode<ShopTimeRelation> {

    @Relationship(direction = Relationship.INCOMING, type = "ShopState")
    private ShopTimeRelation identityRelation;

    @Relationship(direction = Relationship.OUTGOING, type = "PREV")
    private ShopSN previous;

    private String name;
    private String description;

    @Override public ShopTimeRelation getIdentityRelation() {
        return identityRelation;
    }

    @Override public void setIdentityRelation(ShopTimeRelation identityRelation) {
        this.identityRelation = identityRelation;
    }

    public ShopSN getPrevious() {
        return previous;
    }

    public void setPrevious(ShopSN previous) {
        this.previous = previous;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}