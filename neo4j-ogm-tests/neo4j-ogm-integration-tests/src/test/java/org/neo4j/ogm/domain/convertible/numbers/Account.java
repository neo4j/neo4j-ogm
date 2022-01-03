/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.domain.convertible.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.annotation.typeconversion.NumberString;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Gerrit Meier
 * @author Michael J. Simons
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

    @Convert(AbstractListConverter.Base36NumberConverter.class)
    private List<Integer> valueA;

    @Convert(HexadecimalNumberConverter.class)
    private List<Integer> valueB;

    @Convert(FoobarListConverter.class)
    private List<Foobar> listOfFoobars;

    @Convert(AbstractListConverter.FoobarListConverter.class)
    private List<Foobar> anotherListOfFoobars;

    private Integer notConverter;

    @Convert(FoobarConverter.class)
    private Foobar foobar;

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

    public List<Integer> getValueA() {
        return valueA;
    }

    public void setValueA(List<Integer> valueA) {
        this.valueA = valueA;
    }

    public List<Integer> getValueB() {
        return valueB;
    }

    public void setValueB(List<Integer> valueB) {
        this.valueB = valueB;
    }

    public List<Foobar> getListOfFoobars() {
        return listOfFoobars;
    }

    public void setListOfFoobars(List<Foobar> listOfFoobars) {
        this.listOfFoobars = listOfFoobars;
    }

    public Integer getNotConverter() {
        return notConverter;
    }

    public void setNotConverter(Integer notConverter) {
        this.notConverter = notConverter;
    }

    public Foobar getFoobar() {
        return foobar;
    }

    public void setFoobar(Foobar foobar) {
        this.foobar = foobar;
    }

    public List<Foobar> getAnotherListOfFoobars() {
        return anotherListOfFoobars;
    }

    public void setAnotherListOfFoobars(List<Foobar> anotherListOfFoobars) {
        this.anotherListOfFoobars = anotherListOfFoobars;
    }
}
