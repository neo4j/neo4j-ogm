/*
 * Copyright (c) 2002-2021 "Neo4j,"
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.domain.convertible.numbers.Account;
import org.neo4j.ogm.domain.convertible.numbers.Foobar;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.music.TimeHolder;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Michael J. Simons
 */
public class TypeConversionIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.music",
            "org.neo4j.ogm.domain.convertible.numbers");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
    }

    @After
    public void clear() {
        session.purgeDatabase();
    }

    @Test // GH-71
    public void convertibleReturnTypesShouldBeHandled() {
        final ZoneId utc = ZoneId.of("UTC");

        final Artist queen = new Artist("Queen");
        Album album = new Album("Queen");
        album.setArtist(queen);
        album.setRecordedAt(Date.from(LocalDate.of(1972, 11, 30).atStartOfDay(utc).toInstant()));
        album.setReleased(Date.from(LocalDate.of(1973, 7, 13).atStartOfDay(utc).toInstant()));

        album = new Album("Queen II");
        album.setArtist(queen);
        album.setRecordedAt(Date.from(LocalDate.of(1973, 8, 1).atStartOfDay(utc).toInstant()));
        final Date queen2ReleaseDate = Date.from(LocalDate.of(1974, 3, 8).atStartOfDay(utc).toInstant());
        album.setReleased(queen2ReleaseDate);

        session.save(album);
        session.clear();

        final Date latestReleases = session
            .queryForObject(Date.class, "MATCH (n:`l'album`) RETURN MAX(n.releasedAt)", new HashMap<>());
        assertThat(latestReleases).isEqualTo(queen2ReleaseDate);
    }

    @Test // GH-766
    public void savedTimestampAsMappingIsReadBackAsIs() {

        OffsetDateTime someTime = OffsetDateTime.parse("2024-05-01T21:18:15.650+07:00");
        LocalDateTime someLocalDateTime = LocalDateTime.parse("2024-05-01T21:18:15");
        LocalDate someLocalDate = LocalDate.parse("2024-05-01");

        TimeHolder timeHolder = new TimeHolder();
        timeHolder.setSomeTime(someTime);
        timeHolder.setSomeLocalDateTime(someLocalDateTime);
        timeHolder.setSomeLocalDate(someLocalDate);

        session.save(timeHolder);

        verify(timeHolder.getGraphId(), someTime, someLocalDateTime, someLocalDate);
    }

    @Test // GH-766
    public void savedTimestampAsParameterToBatchedCreateIsReadBackAsIs() {

        OffsetDateTime someTime = OffsetDateTime.parse("2024-05-01T21:18:15.650+07:00");
        LocalDateTime someLocalDateTime = LocalDateTime.parse("2024-05-01T21:18:15");
        LocalDate someLocalDate = LocalDate.parse("2024-05-01");

        Map<String, Object> props = new HashMap<>();
        props.put("someTime", someTime);
        props.put("someLocalDateTime", someLocalDateTime);
        props.put("someLocalDate", someLocalDate);

        Map<String, Object> row = new HashMap<>();
        row.put("nodeRef", -1);
        row.put("props", props);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "node");
        parameters.put("rows", Collections.singletonList(row));

        Result result = session.query(
            "UNWIND $rows AS row CREATE (n:`Data`) SET n=row.props RETURN row.nodeRef AS ref, id(n) AS id, $type AS type",
            parameters);

        verify((Long) result.queryResults().iterator().next().get("id"), someTime, someLocalDateTime, someLocalDate);
    }

    @Test // GH-766
    public void dataStoredInNotRealIsoFormatShouldStillBeParsed() {

        OffsetDateTime someTime1 = OffsetDateTime.parse("2024-05-01T21:18:15.650+07:00");
        OffsetDateTime someTime2 = OffsetDateTime.parse("2024-05-01T21:18:15.65+07:00");
        OffsetDateTime someTime3 = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            .parse("2024-05-01T21:18:15.650+07:00", OffsetDateTime::from);
        OffsetDateTime someTime4 = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            .parse("2024-05-01T21:18:15.65+07:00", OffsetDateTime::from);

        assertThat(someTime1).isEqualTo(someTime2);
        assertThat(someTime2).isEqualTo(someTime3);
        assertThat(someTime3).isEqualTo(someTime4);

        String withDifferentMillis = "2024-05-01T21:18:15.651+07:00";
        OffsetDateTime a = OffsetDateTime.parse(withDifferentMillis);
        OffsetDateTime b = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(withDifferentMillis, OffsetDateTime::from);
        assertThat(a).isEqualTo(b);
    }

    @Test // GH-766
    public void savedTimestampAsParameterToSimpleCreateIsReadBackAsIs() {

        OffsetDateTime someTime = OffsetDateTime.parse("2024-05-01T21:18:15.650+07:00");
        LocalDateTime someLocalDateTime = LocalDateTime.parse("2024-05-01T21:18:15");
        LocalDate someLocalDate = LocalDate.parse("2024-05-01");

        Map<String, Object> props = new HashMap<>();
        props.put("someTime", someTime);
        props.put("someLocalDateTime", someLocalDateTime);
        props.put("someLocalDate", someLocalDate);
        TimeHolder timeHolder = session.queryForObject(
            TimeHolder.class,
            "CREATE (d:Data {someTime: $someTime, someLocalDateTime: $someLocalDateTime, someLocalDate: $someLocalDate}) RETURN d",
            props
        );
        verify(timeHolder.getGraphId(), someTime, someLocalDateTime, someLocalDate);
    }

    private void verify(Long graphId, OffsetDateTime expectedOffsetDateTime, LocalDateTime expectedLocalDateTime, LocalDate expectedLocalDate) {

        // opening a new Session to prevent shared data
        TimeHolder reloaded = sessionFactory.openSession().load(TimeHolder.class, graphId);

        assertThat(reloaded.getSomeTime()).isEqualTo(expectedOffsetDateTime);
        assertThat(reloaded.getSomeLocalDateTime()).isEqualTo(expectedLocalDateTime);
        assertThat(reloaded.getSomeLocalDate()).isEqualTo(expectedLocalDate);

        String offsetDateTimeValue = null;
        String localDateTimeValue = null;
        String localDateValue = null;
        if (isBoltDriver() || isHttpDriver()) {

            try(Driver driver = getBoltConnection()) {
                try (org.neo4j.driver.Session driverSession = driver.session()) {
                    Record record = driverSession
                        .run("MATCH (n) WHERE id(n) = $id RETURN n", Values.parameters("id", graphId)).single();

                    Value n = record.get("n");
                    offsetDateTimeValue = n.get("someTime").asString();
                    localDateTimeValue = n.get("someLocalDateTime").asString();
                    localDateValue = n.get("someLocalDate").asString();
                }
            }
        } else if (isEmbeddedDriver()) {

            GraphDatabaseService graphDatabaseService = getDriver().unwrap(GraphDatabaseService.class);
            try (Transaction tx = graphDatabaseService.beginTx()) {

                Node node = tx.getNodeById(graphId);
                offsetDateTimeValue = node.getProperty("someTime").toString();
                localDateTimeValue = node.getProperty("someLocalDateTime").toString();
                localDateValue = node.getProperty("someLocalDate").toString();
            }
        }

        String expectedStringValue;

        expectedStringValue = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(expectedOffsetDateTime);
        assertThat(offsetDateTimeValue).isEqualTo(expectedStringValue);

        expectedStringValue = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(expectedLocalDateTime);
        assertThat(localDateTimeValue).isEqualTo(expectedStringValue);

        expectedStringValue = DateTimeFormatter.ISO_LOCAL_DATE.format(expectedLocalDate);
        assertThat(localDateValue).isEqualTo(expectedStringValue);
    }

    @Test // GH-845
    public void converterShouldBeAppliedBothWaysCorrectly() {

        Session localSession = sessionFactory.openSession();

        Account account = new Account();
        account.setValueA(Collections.singletonList(23));
        account.setValueB(Collections.singletonList(23));
        account.setListOfFoobars(Arrays.asList(new Foobar("A"), new Foobar("B")));
        account.setAnotherListOfFoobars(Arrays.asList(new Foobar("C"), new Foobar("D")));
        account.setFoobar(new Foobar("Foobar"));
        account.setNotConverter(4711);
        localSession.save(account);

        localSession.clear();
        localSession = sessionFactory.openSession();

        Iterable<Map<String, Object>> result = localSession.query(""
                + "MATCH (a:Account) WHERE id(a) = $id "
                + "RETURN a.valueA AS va, a.valueB as vb, a.listOfFoobars as listOfFoobars, a.anotherListOfFoobars as anotherListOfFoobars, a.foobar as foobar, a.notConverter as notConverter",
            Collections.singletonMap("id", account.getId())
        );
        assertThat(result).hasSize(1);
        assertThat(result).first().satisfies(m -> {
            assertThat(m).containsEntry("va", "n");
            assertThat(m).containsEntry("vb", "17");
            assertThat(m).containsEntry("listOfFoobars", "A,B");
            assertThat(m).containsEntry("anotherListOfFoobars", "C,D");
            assertThat(m).containsEntry("foobar", "Foobar");
            assertThat(m).containsEntry("notConverter", 4711L);
        });

        localSession.clear();
        localSession = sessionFactory.openSession();

        account = localSession.load(Account.class, account.getId());
        assertThat(account).isNotNull();
        assertThat(account.getValueA()).containsExactly(23);
        assertThat(account.getValueB()).containsExactly(23);
        assertThat(account.getListOfFoobars().stream().map(Foobar::getValue)).containsExactlyInAnyOrder("A", "B");
        assertThat(account.getAnotherListOfFoobars().stream().map(Foobar::getValue))
            .containsExactlyInAnyOrder("C", "D");
        assertThat(account.getFoobar().getValue()).isEqualTo("Foobar");
    }
}
