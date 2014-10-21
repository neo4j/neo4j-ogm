package org.neo4j.ogm.mapper.domain.forum;

import org.neo4j.ogm.annotation.Label;
import org.neo4j.ogm.annotation.Property;

@Label
public abstract class Membership implements IMembership {

    @Property(name="annualFees")
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
