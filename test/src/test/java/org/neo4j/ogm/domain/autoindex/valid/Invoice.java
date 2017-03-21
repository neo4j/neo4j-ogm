package org.neo4j.ogm.domain.autoindex.valid;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

/**
 * @author Mark Angrish
 */
@NodeEntity
public class Invoice {

    @GraphId
    private Long id;

    @Index(unique = true, primary = true)
    @Property(name = "invoice_number")
    private Long number;

    @Index
    @Property(name = "company_id")
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
