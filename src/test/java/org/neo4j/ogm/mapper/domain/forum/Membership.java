package org.neo4j.ogm.mapper.domain.forum;

import org.neo4j.ogm.annotation.Label;

@Label
public abstract class Membership implements IMembership {

    private Integer fees;
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFees() {
        return fees;
    }

    public void setFees(Integer fees) {
        this.fees = fees;
    }

}
