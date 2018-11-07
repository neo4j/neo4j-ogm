package org.neo4j.ogm.domain.autoindex.invalid;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;

/**
 * @author Mark Angrish
 */
public class BadClass {

    @Id @GeneratedValue
    private Long id;

    @Index(unique = true, primary = true)
    private Long number;

    @Index(unique = true, primary = true)
    private String companyEmail;

    private Long amountInCents;

    public BadClass() {
    }

    public BadClass(Long number, String companyEmail, Long amountInCents) {
        this.number = number;
        this.companyEmail = companyEmail;
        this.amountInCents = amountInCents;
    }

    public Long getId() {
        return id;
    }

    public Long getNumber() {
        return number;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public Long getAmountInCents() {
        return amountInCents;
    }
}
