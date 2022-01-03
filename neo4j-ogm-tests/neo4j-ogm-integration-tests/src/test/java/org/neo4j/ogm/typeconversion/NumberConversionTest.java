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
package org.neo4j.ogm.typeconversion;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.neo4j.ogm.domain.convertible.numbers.Account;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MetaData;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Gerrit Meier
 */
public class NumberConversionTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.numbers");
    private static final ClassInfo accountInfo = metaData.classInfo("Account");

    @Test
    public void assertAccountFieldsHaveDefaultConverters() {
        assertThat(accountInfo.propertyField("balance").hasPropertyConverter()).isTrue();
        assertThat(accountInfo.propertyField("facility").hasPropertyConverter()).isTrue();
        assertThat(accountInfo.propertyField("deposits").hasPropertyConverter()).isTrue();
        assertThat(accountInfo.propertyField("loans").hasPropertyConverter()).isTrue();
    }

    @Test
    public void assertAccountMethodsHaveDefaultConverters() {
        assertThat(accountInfo.propertyField("balance").hasPropertyConverter()).isTrue();

        assertThat(accountInfo.propertyField("facility").hasPropertyConverter()).isTrue();

        assertThat(accountInfo.propertyField("deposits").hasPropertyConverter()).isTrue();

        assertThat(accountInfo.propertyField("loans").hasPropertyConverter()).isTrue();
    }

    @Test
    public void assertAccountBalanceConverterWorks() {

        AttributeConverter converter = accountInfo.propertyField("balance").getPropertyConverter();

        Account account = new Account(new BigDecimal("12345.67"), new BigInteger("1000"));
        assertThat(converter.toGraphProperty(account.getBalance())).isEqualTo("12345.67");

        account.setBalance((BigDecimal) converter.toEntityAttribute("34567.89"));
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("34567.89"));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertAccountDepositConverterWorks() {
        AttributeConverter converter = accountInfo.propertyField("deposits").getPropertyConverter();
        BigDecimal[] deposits = new BigDecimal[] { new BigDecimal("12345.67"), new BigDecimal("34567.89") };
        Account account = new Account(new BigDecimal("12345.67"), new BigInteger("1000"));
        account.setDeposits(deposits);
        String[] convertedDeposits = (String[]) converter.toGraphProperty(account.getDeposits());
        assertThat(convertedDeposits.length).isEqualTo(2);
        assertThat(convertedDeposits[0]).isEqualTo("12345.67");
        assertThat(convertedDeposits[1]).isEqualTo("34567.89");

        account.setDeposits((BigDecimal[]) converter.toEntityAttribute(convertedDeposits));
        assertThat(account.getDeposits()[0]).isEqualTo(new BigDecimal("12345.67"));
        assertThat(account.getDeposits()[1]).isEqualTo(new BigDecimal("34567.89"));
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
        assertThat(convertedLoans.length).isEqualTo(2);
        assertThat(convertedLoans[0]).isEqualTo("123456");
        assertThat(convertedLoans[1]).isEqualTo("567890");

        account.setLoans((List) converter.toEntityAttribute(convertedLoans));
        assertThat(account.getLoans().get(0)).isEqualTo(BigInteger.valueOf(123456));
        assertThat(account.getLoans().get(1)).isEqualTo(BigInteger.valueOf(567890));
    }

    @Test
    public void assertAccountFacilityConverterWorks() {

        AttributeConverter converter = accountInfo.propertyField("facility").getPropertyConverter();

        Account account = new Account(new BigDecimal("12345.67"), new BigInteger("1000"));
        assertThat(converter.toGraphProperty(account.getFacility())).isEqualTo("1000");

        account.setFacility((BigInteger) converter.toEntityAttribute("2000"));
        assertThat(account.getFacility()).isEqualTo(new BigInteger("2000"));
    }

    @Test
    public void assertConvertingNullGraphPropertyWorksCorrectly() {
        AttributeConverter converter = accountInfo.propertyField("facility").getPropertyConverter();
        assertThat(converter.toEntityAttribute(null)).isEqualTo(null);
        converter = accountInfo.propertyField("deposits").getPropertyConverter();
        assertThat(converter.toEntityAttribute(null)).isEqualTo(null);
        converter = accountInfo.propertyField("loans").getPropertyConverter();
        assertThat(converter.toEntityAttribute(null)).isEqualTo(null);
    }

    @Test
    public void assertConvertingNullAttributeWorksCorrectly() {
        AttributeConverter converter = accountInfo.propertyField("facility").getPropertyConverter();
        assertThat(converter.toGraphProperty(null)).isEqualTo(null);
        converter = accountInfo.propertyField("deposits").getPropertyConverter();
        assertThat(converter.toGraphProperty(null)).isEqualTo(null);
        converter = accountInfo.propertyField("loans").getPropertyConverter();
        assertThat(converter.toGraphProperty(null)).isEqualTo(null);
    }

    @Test
    public void assertHasCompositeConverter() {
        MetaData restaurantMetadata = new MetaData("org.neo4j.ogm.domain.restaurant");
        ClassInfo restaurantInfo = restaurantMetadata.classInfo("Restaurant");
        assertThat(restaurantInfo.propertyField("location").hasCompositeConverter()).isTrue();
    }

    @Test(expected = RuntimeException.class)
    public void assertConvertingEmptyGraphPropertyFails() {
        AttributeConverter converter = accountInfo.propertyField("futureBalance").getPropertyConverter();
        assertThat(converter.toEntityAttribute("")).isEqualTo(null);
    }

    @Test
    public void assertConvertingEmptyGraphPropertyWorksCorrectlyWithLenientConverter() {
        AttributeConverter converter = accountInfo.propertyField("futureBalanceLenient").getPropertyConverter();
        assertThat(converter.toEntityAttribute("")).isEqualTo(null);
    }
}
