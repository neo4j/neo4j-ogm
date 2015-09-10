package org.neo4j.ogm.domain.temporalNetwork;

import org.neo4j.ogm.annotation.NodeEntity;

import java.util.HashSet;
import java.util.Set;

@NodeEntity(label = "ShopIN")
public class ShopIN extends AbstractIdentityNode<ShopTimeRelation> {

    private Set<ShopTimeRelation> states = new HashSet<>();

    @Override public Set<ShopTimeRelation> getStates() {
        return states;
    }

    @Override public void setStates(Set<ShopTimeRelation> states) {
        this.states = states;
    }
}
