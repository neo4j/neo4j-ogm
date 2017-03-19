/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.typeconversion;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.ogm.domain.convertible.numbers.Account;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MetaData;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class NumberConversionTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.numbers");
    private static final ClassInfo accountInfo = metaData.classInfo("Account");

    @Test
    public void assertAccountFieldsHaveDefaultConverters() {
        assertTrue(accountInfo.propertyField("balance").hasPropertyConverter());
        assertTrue(accountInfo.propertyField("facility").hasPropertyConverter());
        assertTrue(accountInfo.propertyField("deposits").hasPropertyConverter());
        assertTrue(accountInfo.propertyField("loans").hasPropertyConverter());
    }

    @Test
    public void assertAccountMethodsHaveDefaultConverters() {
        assertTrue(accountInfo.propertyField("balance").hasPropertyConverter());

        assertTrue(accountInfo.propertyField("facility").hasPropertyConverter());

        assertTrue(accountInfo.propertyField("deposits").hasPropertyConverter());

        assertTrue(accountInfo.propertyField("loans").hasPropertyConverter());
    }

    @Test
    public void assertAccountBalanceConverterWorks() {

        AttributeConverter converter = accountInfo.propertyField("balance").getPropertyConverter();

        Account account = new Account(new BigDecimal("12345.67"), new BigInteger("1000"));
        assertEquals("12345.67", converter.toGraphProperty(account.getBalance()));

        account.setBalance((BigDecimal) converter.toEntityAttribute("34567.89"));
        Assert.assertEquals(new BigDecimal("34567.89"), account.getBalance());
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertAccountDepositConverterWorks() {
        AttributeConverter converter = accountInfo.propertyField("deposits").getPropertyConverter();
        BigDecimal[] deposits = new BigDecimal[]{new BigDecimal("12345.67"), new BigDecimal("34567.89")};
        Account account = new Account(new BigDecimal("12345.67"), new BigInteger("1000"));
        account.setDeposits(deposits);
        String[] convertedDeposits = (String[]) converter.toGraphProperty(account.getDeposits());
        assertEquals(2, convertedDeposits.length);
        assertEquals("12345.67", convertedDeposits[0]);
        assertEquals("34567.89", convertedDeposits[1]);

        account.setDeposits((BigDecimal[]) converter.toEntityAttribute(convertedDeposits));
        Assert.assertEquals(new BigDecimal("12345.67"), account.getDeposits()[0]);
        Assert.assertEquals(new BigDecimal("34567.89"), account.getDeposits()[1]);
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertAccountLoNAConverterWorks() {
        AttributeConverter converter = accountInfo.propertyField("loans").getPropertyConverter();
        List<BigInteger> loans = new ArrayList<>();
        loans.add(BigInteger.valueOf(123456));
        loans.add(BigInteger.valueOf(567890));
        Account account = new Account(new BigDecimal("12345.67"), new BigInteger("1000"));
        account.setLoans(loans);
        String[] convertedLoans = (String[]) converter.toGraphProperty(account.getLoans());
        assertEquals(2, convertedLoans.length);
        assertEquals("123456", convertedLoans[0]);
        assertEquals("567890", convertedLoans[1]);

        account.setLoans((List) converter.toEntityAttribute(convertedLoans));
        Assert.assertEquals(BigInteger.valueOf(123456), account.getLoans().get(0));
        Assert.assertEquals(BigInteger.valueOf(567890), account.getLoans().get(1));
    }

    @Test
    public void assertAccountFacilityConverterWorks() {

        AttributeConverter converter = accountInfo.propertyField("facility").getPropertyConverter();

        Account account = new Account(new BigDecimal("12345.67"), new BigInteger("1000"));
        assertEquals("1000", converter.toGraphProperty(account.getFacility()));

        account.setFacility((BigInteger) converter.toEntityAttribute("2000"));
        Assert.assertEquals(new BigInteger("2000"), account.getFacility());
    }

    @Test
    public void assertConvertingNullGraphPropertyWorksCorrectly() {
        AttributeConverter converter = accountInfo.propertyField("facility").getPropertyConverter();
        assertEquals(null, converter.toEntityAttribute(null));
        converter = accountInfo.propertyField("deposits").getPropertyConverter();
        assertEquals(null, converter.toEntityAttribute(null));
        converter = accountInfo.propertyField("loans").getPropertyConverter();
        assertEquals(null, converter.toEntityAttribute(null));
    }

    @Test
    public void assertConvertingNullAttributeWorksCorrectly() {
        AttributeConverter converter = accountInfo.propertyField("facility").getPropertyConverter();
        assertEquals(null, converter.toGraphProperty(null));
        converter = accountInfo.propertyField("deposits").getPropertyConverter();
        assertEquals(null, converter.toGraphProperty(null));
        converter = accountInfo.propertyField("loans").getPropertyConverter();
        assertEquals(null, converter.toGraphProperty(null));
    }

    @Test
    public void assertHasCompositeConverter() {
        MetaData metaData = new MetaData("org.neo4j.ogm.domain.restaurant");
        ClassInfo restaurantInfo = metaData.classInfo("Restaurant");
        assertTrue(restaurantInfo.propertyField("location").hasCompositeConverter());
    }
}
