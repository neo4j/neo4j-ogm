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
package org.neo4j.ogm.persistence.types.nativetypes;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.neo4j.driver.internal.util.ServerVersion;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.types.TypeSystem;
import org.neo4j.harness.TestServerBuilders;
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

    private static URI boltURI = TestServerBuilders.newInProcessBuilder().newServer().boltURI();

    @BeforeClass
    public static void init() {

        Configuration ogmConfiguration = new Configuration.Builder()
            .uri(boltURI.toString())
            .encryptionLevel(Config.EncryptionLevel.NONE.name())
            .useNativeTypes()
            .build();

        BoltDriver boltOgmDriver = new BoltDriver();
        boltOgmDriver.configure(ogmConfiguration);
        sessionFactory = new SessionFactory(boltOgmDriver, DatesBoltTest.class.getPackage().getName());
    }

    @Override
    public void convertPersistAndLoadTemporalAmounts()  {
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

        assumeTrue(driverSupportsLocalDate());

        try (
            Driver driver = GraphDatabase.driver(boltURI, Config.build().withoutEncryption().toConfig());
        ) {
            assumeTrue(databaseSupportJava8TimeTypes(driver));

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

    private static boolean driverSupportsLocalDate() {

        Class<TypeSystem> t = TypeSystem.class;
        try {
            return t.getDeclaredMethod("LOCAL_DATE_TIME") != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean databaseSupportJava8TimeTypes(Driver driver) {
        return ServerVersion.version(driver)
            .greaterThanOrEqual(ServerVersion.version("3.4.0"));
    }
}
