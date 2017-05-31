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
package org.neo4j.ogm.persistence.types.convertible;


import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.convertible.date.Java8DatesMemo;
import org.neo4j.ogm.domain.convertible.date.Memo;
import org.neo4j.ogm.domain.convertible.enums.Education;
import org.neo4j.ogm.domain.convertible.enums.Gender;
import org.neo4j.ogm.domain.convertible.enums.Person;
import org.neo4j.ogm.domain.convertible.numbers.Account;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Luanne Misquitta
 */
public class ConvertibleIntegrationTest extends MultiDriverTestClass {

    private static Session session;

    @BeforeClass
    public static void init() throws IOException {
        session = new SessionFactory(getBaseConfiguration().build(), "org.neo4j.ogm.domain.convertible").openSession();
    }

    @After
    public void teardown() {
        session.purgeDatabase();
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void shouldSaveAndRetrieveEnums() {
        List<Education> completed = new ArrayList<>();
        completed.add(Education.HIGHSCHOOL);
        completed.add(Education.BACHELORS);

        Person person = new Person();
        person.setName("luanne");
        person.setInProgressEducation(new Education[]{Education.MASTERS, Education.PHD});
        person.setCompletedEducation(completed);
        person.setGender(Gender.FEMALE);
        session.save(person);

        Person luanne = session.loadAll(Person.class, new Filter("name", ComparisonOperator.EQUALS, "luanne")).iterator().next();
        assertEquals(Gender.FEMALE, luanne.getGender());
        assertTrue(luanne.getCompletedEducation().contains(Education.HIGHSCHOOL));
        assertTrue(luanne.getCompletedEducation().contains(Education.BACHELORS));
        assertEquals(2, luanne.getInProgressEducation().length);
        assertTrue(luanne.getInProgressEducation()[0].equals(Education.MASTERS) || luanne.getInProgressEducation()[1].equals(Education.MASTERS));
        assertTrue(luanne.getInProgressEducation()[0].equals(Education.PHD) || luanne.getInProgressEducation()[1].equals(Education.PHD));
    }

    @Test
    public void shouldSaveAndRetrieveEnumsAsResult() {
        List<Education> completed = new ArrayList<>();
        completed.add(Education.HIGHSCHOOL);
        completed.add(Education.BACHELORS);

        Person person = new Person();
        person.setName("luanne");
        person.setInProgressEducation(new Education[]{Education.MASTERS, Education.PHD});
        person.setCompletedEducation(completed);
        person.setGender(Gender.FEMALE);
        session.save(person);
        session.clear();

        Result res = session.query("MATCH (p:Person{name:'luanne'}) return p", Collections.EMPTY_MAP);
        Person luanne = (Person) res.queryResults().iterator().next().get("p");
        assertEquals(Gender.FEMALE, luanne.getGender());
        assertTrue(luanne.getCompletedEducation().contains(Education.HIGHSCHOOL));
        assertTrue(luanne.getCompletedEducation().contains(Education.BACHELORS));
        assertEquals(2, luanne.getInProgressEducation().length);
        assertTrue(luanne.getInProgressEducation()[0].equals(Education.MASTERS) || luanne.getInProgressEducation()[1].equals(Education.MASTERS));
        assertTrue(luanne.getInProgressEducation()[0].equals(Education.PHD) || luanne.getInProgressEducation()[1].equals(Education.PHD));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void shouldSaveAndRetrieveDates() throws ParseException {
        SimpleDateFormat simpleDateISO8601format = new SimpleDateFormat(DateString.ISO_8601);
        simpleDateISO8601format.setTimeZone(TimeZone.getTimeZone("UTC"));

        Calendar date0 = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        date0.setTimeInMillis(0);
        Calendar date20000 = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        date20000.setTimeInMillis(20000);
        Set<Date> implementations = new HashSet<>();
        implementations.add(date0.getTime());
        implementations.add(date20000.getTime());

        Calendar date40000 = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        date40000.setTimeInMillis(40000);
        Calendar date100000 = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        date100000.setTimeInMillis(100000);
        Date[] escalations = new Date[]{date40000.getTime(), date100000.getTime()};

        Calendar actioned = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        actioned.setTimeInMillis(20000);

        Memo memo = new Memo();
        memo.setMemo("theMemo");
        memo.setImplementations(implementations);
        memo.setEscalations(escalations);
        memo.setActioned(actioned.getTime());
        memo.setClosed(new Date());
        session.save(memo);

        Memo loadedMemo = session.loadAll(Memo.class, new Filter("memo", ComparisonOperator.EQUALS, "theMemo")).iterator().next();

        Calendar loadedCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        loadedCal.setTime(loadedMemo.getActioned());
        assertEquals(actioned.get(Calendar.DAY_OF_MONTH), loadedCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(actioned.get(Calendar.MONTH), loadedCal.get(Calendar.MONTH));
        assertEquals(actioned.get(Calendar.YEAR), loadedCal.get(Calendar.YEAR));

        assertEquals(2, loadedMemo.getImplementations().size());
        Iterator<Date> implementationsIter = loadedMemo.getImplementations().iterator();
        loadedCal.setTime(implementationsIter.next());
        assertEquals(date0.get(Calendar.DAY_OF_MONTH), loadedCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(date0.get(Calendar.MONTH), loadedCal.get(Calendar.MONTH));
        assertEquals(date0.get(Calendar.YEAR), loadedCal.get(Calendar.YEAR));

        loadedCal.setTime(implementationsIter.next());
        assertEquals(date20000.get(Calendar.DAY_OF_MONTH), loadedCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(date20000.get(Calendar.MONTH), loadedCal.get(Calendar.MONTH));
        assertEquals(date20000.get(Calendar.YEAR), loadedCal.get(Calendar.YEAR));

        assertEquals(2, loadedMemo.getEscalations().length);
        loadedCal.setTime(loadedMemo.getEscalations()[0]);
        assertEquals(date40000.get(Calendar.DAY_OF_MONTH), loadedCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(date40000.get(Calendar.MONTH), loadedCal.get(Calendar.MONTH));
        assertEquals(date40000.get(Calendar.YEAR), loadedCal.get(Calendar.YEAR));

        loadedCal.setTime(loadedMemo.getEscalations()[1]);
        assertEquals(date100000.get(Calendar.DAY_OF_MONTH), loadedCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(date100000.get(Calendar.MONTH), loadedCal.get(Calendar.MONTH));
        assertEquals(date100000.get(Calendar.YEAR), loadedCal.get(Calendar.YEAR));
    }

    @Test
    public void shouldSaveAndRetrieveJava8Dates() {

        Instant instant = Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2007-12-03T10:15:30.001+01:00"));

        Java8DatesMemo java8DatesMemo = new Java8DatesMemo(instant);
        session.save(java8DatesMemo);
        session.clear();

        Result result = session.query("MATCH (m:Java8DatesMemo) return m.recorded, m.closed, m.approved", Collections.emptyMap());
        Map<String, Object> record = result.queryResults().iterator().next();
        assertEquals("2007-12-03T09:15:30.001Z", record.get("m.recorded"));
        assertEquals(1196673330001L, record.get("m.closed"));
        assertEquals("2007-12-03", record.get("m.approved"));

        Java8DatesMemo memo = session.loadAll(Java8DatesMemo.class).iterator().next();
        assertEquals(instant, memo.getRecorded());
        assertEquals(instant, memo.getClosed());
        assertEquals(LocalDateTime.ofInstant(instant, ZoneOffset.UTC).toLocalDate(), memo.getApproved());
    }


        /**
		 * @see DATAGRAPH-550
		 */
    @Test
    public void shouldSaveAndRetrieveNumbers() {

        Account account = new Account(new BigDecimal("12345.67"), new BigInteger("1000"));
        account.setCode((short) 1000);

        BigDecimal[] deposits = new BigDecimal[]{new BigDecimal("12345.67"), new BigDecimal("34567.89")};

        List<BigInteger> loans = new ArrayList<>();
        loans.add(BigInteger.valueOf(123456));
        loans.add(BigInteger.valueOf(567890));

        account.setLoans(loans);
        account.setDeposits(deposits);

        session.save(account);

        Account loadedAccount = session.loadAll(Account.class).iterator().next();
        assertEquals((short) 1000, account.getCode());
        assertEquals(new BigDecimal("12345.67"), loadedAccount.getBalance());
        assertEquals(new BigInteger("1000"), loadedAccount.getFacility());
        assertEquals(loans, loadedAccount.getLoans());
        assertSameArray(deposits, loadedAccount.getDeposits());
    }

    /**
     * @see issue #72
     */
    @Test
    public void shouldSaveAndRetrieveIntegerDates() {
        Memo memo = new Memo();
        memo.setClosed(new Date(0));
        session.save(memo);

        memo = session.load(Memo.class, memo.getId());
        assertEquals(new Date(0).getTime(), memo.getClosed().getTime());
    }

    /**
     * @see Issue #77
     */
    @Test
    public void shouldSaveAndRetrieveIntegerFloats() {
        Account account = new Account();
        account.setLimit(10f);
        session.save(account);

        Account loadedAccount = session.load(Account.class, account.getId());
        assertEquals(account.getLimit(), loadedAccount.getLimit());

        account.setLimit(18277.55f);
        session.save(account);
        loadedAccount = session.load(Account.class, account.getId());
        assertEquals(account.getLimit(), loadedAccount.getLimit());
    }

    public void assertSameArray(Object[] as, Object[] bs) {

        if (as == null || bs == null) fail("null arrays not allowed");
        if (as.length != bs.length) fail("arrays are not same length");

        for (Object a : as) {
            boolean found = false;
            for (Object b : bs) {
                if (b.toString().equals(a.toString())) {
                    found = true;
                    break;
                }
            }
            if (!found) fail("array contents are not the same: " + as + ", " + bs);
        }
    }
}
