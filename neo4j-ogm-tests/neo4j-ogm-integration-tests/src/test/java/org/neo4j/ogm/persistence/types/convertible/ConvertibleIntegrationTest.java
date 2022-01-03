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
package org.neo4j.ogm.persistence.types.convertible;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
import org.neo4j.ogm.domain.convertible.numbers.Foobar;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class ConvertibleIntegrationTest extends TestContainersTestBase {

    private static Session session;
    private static SessionFactory sessionFactory;

    @BeforeClass
    public static void init() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.convertible");
        session = sessionFactory.openSession();
    }

    @After
    public void teardown() {
        session.purgeDatabase();
    }

    @Test // DATAGRAPH-550
    public void shouldSaveAndRetrieveEnums() {
        List<Education> completed = new ArrayList<>();
        completed.add(Education.HIGHSCHOOL);
        completed.add(Education.BACHELORS);

        Person person = new Person();
        person.setName("luanne");
        person.setInProgressEducation(new Education[] { Education.MASTERS, Education.PHD });
        person.setCompletedEducation(completed);
        person.setGender(Gender.FEMALE);
        session.save(person);

        Person luanne = session.loadAll(Person.class, new Filter("name", ComparisonOperator.EQUALS, "luanne"))
            .iterator().next();
        assertThat(luanne.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(luanne.getCompletedEducation().contains(Education.HIGHSCHOOL)).isTrue();
        assertThat(luanne.getCompletedEducation().contains(Education.BACHELORS)).isTrue();
        assertThat(luanne.getInProgressEducation().length).isEqualTo(2);
        assertThat(luanne.getInProgressEducation()[0].equals(Education.MASTERS) || luanne.getInProgressEducation()[1]
            .equals(Education.MASTERS)).isTrue();
        assertThat(luanne.getInProgressEducation()[0].equals(Education.PHD) || luanne.getInProgressEducation()[1]
            .equals(Education.PHD)).isTrue();
    }

    @Test
    public void shouldSaveAndRetrieveEnumsAsResult() {
        List<Education> completed = new ArrayList<>();
        completed.add(Education.HIGHSCHOOL);
        completed.add(Education.BACHELORS);

        Person person = new Person();
        person.setName("luanne");
        person.setInProgressEducation(new Education[] { Education.MASTERS, Education.PHD });
        person.setCompletedEducation(completed);
        person.setGender(Gender.FEMALE);
        session.save(person);
        session.clear();

        Result res = session.query("MATCH (p:Person{name:'luanne'}) return p", Collections.EMPTY_MAP);
        Person luanne = (Person) res.queryResults().iterator().next().get("p");
        assertThat(luanne.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(luanne.getCompletedEducation().contains(Education.HIGHSCHOOL)).isTrue();
        assertThat(luanne.getCompletedEducation().contains(Education.BACHELORS)).isTrue();
        assertThat(luanne.getInProgressEducation().length).isEqualTo(2);
        assertThat(luanne.getInProgressEducation()[0].equals(Education.MASTERS) || luanne.getInProgressEducation()[1]
            .equals(Education.MASTERS)).isTrue();
        assertThat(luanne.getInProgressEducation()[0].equals(Education.PHD) || luanne.getInProgressEducation()[1]
            .equals(Education.PHD)).isTrue();
    }

    @Test // DATAGRAPH-550 GH-758 GH-771
    public void shouldSaveAndRetrieveDates() {
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
        Date[] escalations = new Date[] { date40000.getTime(), date100000.getTime() };

        Calendar actioned = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        actioned.setTimeInMillis(20000);

        Memo memo = new Memo();
        memo.setMemo("theMemo");
        memo.setImplementations(implementations);
        memo.setEscalations(escalations);
        memo.setActioned(actioned.getTime());
        memo.setClosed(new Date());
        memo.setActionedAsInstant(actioned.toInstant());
        session.save(memo);

        Memo loadedMemo = session.loadAll(Memo.class, new Filter("memo", ComparisonOperator.EQUALS, "theMemo"))
            .iterator().next();

        Calendar loadedCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        loadedCal.setTime(loadedMemo.getActioned());
        assertThat(loadedCal.get(Calendar.DAY_OF_MONTH)).isEqualTo(actioned.get(Calendar.DAY_OF_MONTH));
        assertThat(loadedCal.get(Calendar.MONTH)).isEqualTo(actioned.get(Calendar.MONTH));
        assertThat(loadedCal.get(Calendar.YEAR)).isEqualTo(actioned.get(Calendar.YEAR));
        assertThat(loadedMemo.getActionedAsInstant()).isEqualTo(actioned.toInstant());

        assertThat(loadedMemo.getImplementations()).hasSize(2);
        Iterator<Date> implementationsIter = loadedMemo.getImplementations().iterator();
        loadedCal.setTime(implementationsIter.next());
        assertThat(loadedCal.get(Calendar.DAY_OF_MONTH)).isEqualTo(date0.get(Calendar.DAY_OF_MONTH));
        assertThat(loadedCal.get(Calendar.MONTH)).isEqualTo(date0.get(Calendar.MONTH));
        assertThat(loadedCal.get(Calendar.YEAR)).isEqualTo(date0.get(Calendar.YEAR));

        loadedCal.setTime(implementationsIter.next());
        assertThat(loadedCal.get(Calendar.DAY_OF_MONTH)).isEqualTo(date20000.get(Calendar.DAY_OF_MONTH));
        assertThat(loadedCal.get(Calendar.MONTH)).isEqualTo(date20000.get(Calendar.MONTH));
        assertThat(loadedCal.get(Calendar.YEAR)).isEqualTo(date20000.get(Calendar.YEAR));

        assertThat(loadedMemo.getEscalations().length).isEqualTo(2);
        loadedCal.setTime(loadedMemo.getEscalations()[0]);
        assertThat(loadedCal.get(Calendar.DAY_OF_MONTH)).isEqualTo(date40000.get(Calendar.DAY_OF_MONTH));
        assertThat(loadedCal.get(Calendar.MONTH)).isEqualTo(date40000.get(Calendar.MONTH));
        assertThat(loadedCal.get(Calendar.YEAR)).isEqualTo(date40000.get(Calendar.YEAR));

        loadedCal.setTime(loadedMemo.getEscalations()[1]);
        assertThat(loadedCal.get(Calendar.DAY_OF_MONTH)).isEqualTo(date100000.get(Calendar.DAY_OF_MONTH));
        assertThat(loadedCal.get(Calendar.MONTH)).isEqualTo(date100000.get(Calendar.MONTH));
        assertThat(loadedCal.get(Calendar.YEAR)).isEqualTo(date100000.get(Calendar.YEAR));
    }

    @Test // GH-771
    public void instantsWithCustomFormatAndTZShouldWork() {

        Memo memo = new Memo();
        memo.setMemo("theMemo");
        Instant actionedInstant = ZonedDateTime.of(2020, 3, 6, 16, 6, 23, 0, ZoneId.of("Europe/Berlin")).toInstant();
        memo.setActionedAsInstant(actionedInstant);
        memo.setActionedAsInstantWithCustomFormat1(actionedInstant);
        memo.setActionedAsInstantWithCustomFormat2(actionedInstant);
        session.save(memo);

        Memo loadedMemo = session.loadAll(Memo.class, new Filter("memo", ComparisonOperator.EQUALS, "theMemo"))
            .iterator().next();

        assertThat(loadedMemo.getActionedAsInstantWithCustomFormat1()).isEqualTo(actionedInstant);
        assertThat(loadedMemo.getActionedAsInstantWithCustomFormat2()).isEqualTo(actionedInstant);

        Iterable<Map<String, Object>> results = session.query(
            "MATCH (m:Memo) WHERE id(m) = $id RETURN m.actionedAsInstantWithCustomFormat1 as a1, m.actionedAsInstantWithCustomFormat2 as a2",
            Collections.singletonMap("id", loadedMemo.getId())).queryResults();
        assertThat(results).hasSize(1);
        Map<String, Object> result = results.iterator().next();
        assertThat(result).containsEntry("a1", "2020-03-06 15:06:23");
        assertThat(result).containsEntry("a2", "2020-03-06 16:06:23");
    }

    @Test
    public void shouldSaveAndRetrieveJava8Dates() {

        Instant instant = Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2007-12-03T10:15:30.001+01:00"));

        Java8DatesMemo java8DatesMemo = new Java8DatesMemo(instant);
        session.save(java8DatesMemo);
        session.clear();

        Result result = session
            .query("MATCH (m:Java8DatesMemo) return m.recorded, m.closed, m.approved", Collections.emptyMap());
        Map<String, Object> record = result.queryResults().iterator().next();
        assertThat(record.get("m.recorded")).isEqualTo("2007-12-03T09:15:30.001Z");
        assertThat(record.get("m.closed")).isEqualTo(1196673330001L);
        assertThat(record.get("m.approved")).isEqualTo("2007-12-03");

        Java8DatesMemo memo = session.loadAll(Java8DatesMemo.class).iterator().next();
        assertThat(memo.getRecorded()).isEqualTo(instant);
        assertThat(memo.getClosed()).isEqualTo(instant);
        assertThat(memo.getApproved()).isEqualTo(LocalDateTime.ofInstant(instant, ZoneOffset.UTC).toLocalDate());
    }

    @Test
    public void shouldSaveListOfLocalDate() {

        Java8DatesMemo memo = new Java8DatesMemo();

        LocalDate now = LocalDate.of(2017, 7, 23);
        LocalDate tomorrow = now.plusDays(1);
        List<LocalDate> dateList = Arrays.asList(now, tomorrow);
        memo.setDateList(dateList);

        session.save(memo);
        session.clear();

        Result result = session.query("MATCH (m:Java8DatesMemo) return m.dateList", Collections.emptyMap());
        Map<String, Object> record = result.queryResults().iterator().next();
        String[] dateArray = (String[]) record.get("m.dateList");
        assertThat(dateArray).isEqualTo(new String[] { "2017-07-23", "2017-07-24" });

        Java8DatesMemo loaded = session.load(Java8DatesMemo.class, memo.getId());
        assertThat(loaded.getDateList()).isEqualTo(dateList);
    }

    @Test
    public void shouldSaveLocalDateTime() {

        Java8DatesMemo memo = new Java8DatesMemo();

        LocalDateTime dateTime = LocalDateTime.of(2017, 7, 23, 1, 2, 3);
        memo.setDateTime(dateTime);

        session.save(memo);
        session.clear();

        Result result = session.query("MATCH (m:Java8DatesMemo) return m.dateTime", Collections.emptyMap());
        Map<String, Object> record = result.queryResults().iterator().next();
        assertThat(record.get("m.dateTime")).isEqualTo("2017-07-23T01:02:03");

        Java8DatesMemo loaded = session.load(Java8DatesMemo.class, memo.getId());
        assertThat(loaded.getDateTime()).isEqualTo(dateTime);
    }

    @Test
    public void shouldSaveListOfLocalDateTime() {

        Java8DatesMemo memo = new Java8DatesMemo();

        LocalDateTime dateTime = LocalDateTime.of(2017, 7, 23, 1, 2, 3);
        LocalDateTime dateTime1 = dateTime.plusDays(1);
        List<LocalDateTime> dateTimeList = Arrays.asList(dateTime, dateTime1);
        memo.setDateTimeList(dateTimeList);

        session.save(memo);
        session.clear();

        Result result = session.query("MATCH (m:Java8DatesMemo) return m.dateTimeList", Collections.emptyMap());
        Map<String, Object> record = result.queryResults().iterator().next();
        String[] dateArray = (String[]) record.get("m.dateTimeList");
        assertThat(dateArray).isEqualTo(new String[] { "2017-07-23T01:02:03", "2017-07-24T01:02:03" });

        Java8DatesMemo loaded = session.load(Java8DatesMemo.class, memo.getId());
        assertThat(loaded.getDateTimeList()).isEqualTo(dateTimeList);
    }

    @Test
    public void shouldSaveOffsetDateTime() {

        Java8DatesMemo memo = new Java8DatesMemo();

        LocalDateTime dateTime = LocalDateTime.of(2017, 7, 23, 1, 2, 3);
        OffsetDateTime offsetDateTime = OffsetDateTime.of(dateTime, ZoneOffset.ofHours(1));
        memo.setOffsetDateTime(offsetDateTime);

        session.save(memo);
        session.clear();

        Result result = session.query("MATCH (m:Java8DatesMemo) return m.offsetDateTime", Collections.emptyMap());
        Map<String, Object> record = result.queryResults().iterator().next();
        assertThat(record.get("m.offsetDateTime")).isEqualTo("2017-07-23T01:02:03+01:00");

        Java8DatesMemo loaded = session.load(Java8DatesMemo.class, memo.getId());
        assertThat(loaded.getOffsetDateTime()).isEqualTo(offsetDateTime);
    }

    @Test
    public void shouldSaveOffsetDateTimeList() {
        Java8DatesMemo memo = new Java8DatesMemo();

        LocalDateTime dateTime = LocalDateTime.of(2017, 7, 23, 1, 2, 3);
        OffsetDateTime offsetDateTime = OffsetDateTime.of(dateTime, ZoneOffset.ofHours(1));
        OffsetDateTime offsetDateTime1 = OffsetDateTime.of(dateTime.plusDays(1), ZoneOffset.ofHours(1));
        List<OffsetDateTime> offsetDateTimeList = Arrays.asList(offsetDateTime, offsetDateTime1);
        memo.setOffsetDateTimeList(offsetDateTimeList);

        session.save(memo);
        session.clear();

        Result result = session.query("MATCH (m:Java8DatesMemo) return m.offsetDateTimeList", Collections.emptyMap());
        Map<String, Object> record = result.queryResults().iterator().next();
        String[] dateArray = (String[]) record.get("m.offsetDateTimeList");
        assertThat(dateArray).isEqualTo(new String[] { "2017-07-23T01:02:03+01:00", "2017-07-24T01:02:03+01:00" });

        Java8DatesMemo loaded = session.load(Java8DatesMemo.class, memo.getId());
        assertThat(loaded.getOffsetDateTimeList()).isEqualTo(offsetDateTimeList);
    }

    @Test // DATAGRAPH-550
    public void shouldSaveAndRetrieveNumbers() {

        Account account = new Account(new BigDecimal("12345.67"), new BigInteger("1000"));
        account.setCode((short) 1000);

        BigDecimal[] deposits = new BigDecimal[] { new BigDecimal("12345.67"), new BigDecimal("34567.89") };

        List<BigInteger> loans = new ArrayList<>();
        loans.add(BigInteger.valueOf(123456));
        loans.add(BigInteger.valueOf(567890));

        account.setLoans(loans);
        account.setDeposits(deposits);

        session.save(account);

        Account loadedAccount = session.loadAll(Account.class).iterator().next();
        assertThat(account.getCode()).isEqualTo((short) 1000);
        assertThat(loadedAccount.getBalance()).isEqualTo(new BigDecimal("12345.67"));
        assertThat(loadedAccount.getFacility()).isEqualTo(new BigInteger("1000"));
        assertThat(loadedAccount.getLoans()).isEqualTo(loans);
        assertThat(loadedAccount.getDeposits()).isEqualTo(deposits);
    }

    @Test // GH-880
    public void shouldDeletePropertiesWhenAttributesAreNull() {

        Account account = new Account(new BigDecimal("12345.67"), new BigInteger("1000"));
        account.setFoobar(new Foobar("A thing"));

        Session localSession = sessionFactory.openSession();
        localSession.save(account);
        localSession.clear();

        Map<String, Long> parameters = Collections.singletonMap("id", account.getId());
        String getFooBarAttributeQuery = "MATCH (n:Account) WHERE id(n) = $id RETURN n.foobar";

        String foobbarValue = localSession.queryForObject(String.class, getFooBarAttributeQuery, parameters);
        assertThat(foobbarValue).isEqualTo("A thing");

        account.setFoobar(null);
        localSession = sessionFactory.openSession();
        localSession.save(account);
        localSession.clear();

        foobbarValue = localSession.queryForObject(String.class, getFooBarAttributeQuery, parameters);
        assertThat(foobbarValue).isNull();
    }

    @Test // GH-72
    public void shouldSaveAndRetrieveIntegerDates() {
        Memo memo = new Memo();
        memo.setClosed(new Date(0));
        session.save(memo);

        memo = session.load(Memo.class, memo.getId());
        assertThat(memo.getClosed().getTime()).isEqualTo(new Date(0).getTime());
    }

    @Test // GH-77
    public void shouldSaveAndRetrieveIntegerFloats() {
        Account account = new Account();
        account.setLimit(10f);
        session.save(account);

        Account loadedAccount = session.load(Account.class, account.getId());
        assertThat(loadedAccount.getLimit()).isEqualTo(account.getLimit());

        account.setLimit(18277.55f);
        session.save(account);
        loadedAccount = session.load(Account.class, account.getId());
        assertThat(loadedAccount.getLimit()).isEqualTo(account.getLimit());
    }
}
