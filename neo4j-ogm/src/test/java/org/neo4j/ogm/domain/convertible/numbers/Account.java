package org.neo4j.ogm.domain.convertible.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Account {

    private Long id;
    private BigDecimal balance;
    private BigInteger facility;

    public Account(BigDecimal balance, BigInteger facility) {
        this.balance = balance;
        this.facility = facility;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigInteger getFacility() {
        return facility;
    }

    public void setFacility(BigInteger facility) {
        this.facility = facility;
    }
}
