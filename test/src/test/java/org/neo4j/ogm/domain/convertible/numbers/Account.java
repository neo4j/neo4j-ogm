/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.domain.convertible.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.neo4j.ogm.annotation.typeconversion.NumberString;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Gerrit Meier
 */
public class Account {

    private Long id;
    private BigDecimal balance;
    private BigInteger facility;
    private BigDecimal[] deposits;

    @NumberString(value = Integer.class)
    private Integer futureBalance;

    @NumberString(value = Integer.class, lenient = true)
    private Integer futureBalanceLenient;

    private List<BigInteger> loans;
    private short code;
    private Float limit;

    public Account() {
    }

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

    public BigDecimal[] getDeposits() {
        return deposits;
    }

    public void setDeposits(BigDecimal[] deposits) {
        this.deposits = deposits;
    }

    public Integer getFutureBalance() {
        return futureBalance;
    }

    public void setFutureBalance(Integer futureBalance) {
        this.futureBalance = futureBalance;
    }

    public Integer getFutureBalanceLenient() {
        return futureBalanceLenient;
    }

    public void setFutureBalanceLenient(Integer futureBalanceLenient) {
        this.futureBalanceLenient = futureBalanceLenient;
    }

    public List<BigInteger> getLoans() {
        return loans;
    }

    public void setLoans(List<BigInteger> loans) {
        this.loans = loans;
    }

    public short getCode() {
        return code;
    }

    public void setCode(short code) {
        this.code = code;
    }

    public Float getLimit() {
        return limit;
    }

    public void setLimit(Float limit) {
        this.limit = limit;
    }

    public Long getId() {
        return id;
    }
}
