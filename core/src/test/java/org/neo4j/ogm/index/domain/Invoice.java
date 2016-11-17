package org.neo4j.ogm.index.domain;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author Mark Angrish
 */
@NodeEntity
public class Invoice {

    @GraphId
    private Long id;

    @Index(unique = true, primary = true)
    private Long number;

    @Index
    private String company;

    private Long amountInCents;

    public Invoice() {
    }

    public Invoice(Long number, String company, Long amountInCents) {
        this.number = number;
        this.company = company;
        this.amountInCents = amountInCents;
    }

    public Long getId() {
        return id;
    }

    public Long getNumber() {
        return number;
    }

    public String getCompany() {
        return company;
    }

    public Long getAmountInCents() {
        return amountInCents;
    }
}
