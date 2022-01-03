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
package org.neo4j.ogm.persistence.types.nativetypes;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public class DatesBoltTest extends DatesTestBase {

    @BeforeClass
    public static void init() {

        assumeTrue(isBoltDriver());

        Configuration ogmConfiguration = getBaseConfigurationBuilder()
            .useNativeTypes()
            .build();

        BoltDriver boltOgmDriver = new BoltDriver();
        boltOgmDriver.configure(ogmConfiguration);
        sessionFactory = new SessionFactory(boltOgmDriver, DatesBoltTest.class.getPackage().getName());
    }

    @Override
    public void convertPersistAndLoadTemporalAmounts() {
        Session session = sessionFactory.openSession();

        long id = session.queryForObject(
            Long.class,
            "CREATE (s:`DatesTestBase$Sometime` {temporalAmount: duration('P13Y370M45DT25H120M')}) RETURN id(s)",
            Collections.emptyMap());
        session.clear();
        Sometime loaded = session.load(Sometime.class, id);
        assertThat(loaded.getTemporalAmount()).isEqualTo(Values.isoDuration(526, 45, 97200, 0).asIsoDuration());
    }

    @Override
    public void shouldUseNativeDateTimeTypesInParameterMaps() {

        try (
            Driver driver = GraphDatabase.driver(getBoltUrl())
        ) {
            Session session = sessionFactory.openSession();

            Transaction transaction = session.beginTransaction();
            LocalDate localDate = LocalDate.of(2018, 11, 14);
            LocalDateTime localDateTime = LocalDateTime.of(2018, 10, 11, 15, 24);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("a", localDate);
            parameters.put("b", localDateTime);
            session.query("CREATE (n:Test {a: $a, b: $b})", parameters);
            transaction.commit();

            Record record = driver.session().run("MATCH (n:Test) RETURN n.a, n.b").single();
            Object a = record.get("n.a").asObject();
            assertThat(a).isInstanceOf(LocalDate.class)
                .isEqualTo(localDate);

            Object b = record.get("n.b").asObject();
            assertThat(b).isInstanceOf(LocalDateTime.class)
                .isEqualTo(localDateTime);
        }
    }

    @Override
    public void shouldObeyExplicitConversionOfNativeTypes() {

        Map<String, Object> params = createSometimeWithConvertedLocalDate();
        try (
            Driver driver = GraphDatabase.driver(getBoltUrl())
        ) {
            Record record = driver.session()
                .run(
                    "MATCH (n:`DatesTestBase$Sometime`) WHERE id(n) = $id RETURN n.convertedLocalDate as convertedLocalDate",
                    params).next();

            Object a = record.get("convertedLocalDate").asObject();
            assertThat(a).isInstanceOf(String.class)
                .isEqualTo("2018-11-21");
        }
    }
}
