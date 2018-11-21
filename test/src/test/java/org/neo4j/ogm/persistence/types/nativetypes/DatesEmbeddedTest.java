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

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.util.Files;
import org.junit.BeforeClass;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.values.storable.DurationValue;

/**
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public class DatesEmbeddedTest extends DatesTestBase {

    private static EmbeddedDriver embeddedOgmDriver;

    @BeforeClass
    public static void init() {

        File temporaryFolder = Files.newTemporaryFolder();
        temporaryFolder.deleteOnExit();

        Configuration ogmConfiguration = new Configuration.Builder()
            .uri("file://" + temporaryFolder.getAbsolutePath())
            .useNativeTypes()
            .build();

        embeddedOgmDriver = new EmbeddedDriver();
        embeddedOgmDriver.configure(ogmConfiguration);
        sessionFactory = new SessionFactory(embeddedOgmDriver, SpatialEmbeddedTest.class.getPackage().getName());
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

        assertThat(loaded.getTemporalAmount()).isEqualTo(DurationValue.parse("P13Y370M45DT25H120M"));
    }

    @Override
    public void shouldUseNativeDateTimeTypesInParameterMaps() {
        Session session = sessionFactory.openSession();

        LocalDate localDate = LocalDate.of(2018, 11, 14);
        LocalDateTime localDateTime = LocalDateTime.of(2018, 10, 11, 15, 24);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("a", localDate);
        parameters.put("b", localDateTime);
        session.query("CREATE (n:Test {a: $a, b: $b})", parameters);

        Map<String, Object> result = embeddedOgmDriver.getGraphDatabaseService()
            .execute("MATCH (n:Test) RETURN n.a, n.b").next();

        Object a = result.get("n.a");
        assertThat(a).isInstanceOf(LocalDate.class)
            .isEqualTo(localDate);

        Object b = result.get("n.b");
        assertThat(b).isInstanceOf(LocalDateTime.class)
            .isEqualTo(localDateTime);
    }

    @Override
    public void shouldObeyExplicitConversionOfNativeTypes() {

        Map<String, Object> params = createSometimeWithConvertedLocalDate();
        Map<String, Object> result = embeddedOgmDriver.getGraphDatabaseService()
            .execute(
                "MATCH (n:`DatesTestBase$Sometime`) WHERE id(n) = $id RETURN n.convertedLocalDate AS convertedLocalDate",
                params).next();

        Object a = result.get("convertedLocalDate");
        assertThat(a).isInstanceOf(String.class)
            .isEqualTo("2018-11-21");
    }

    private static boolean databaseSupportJava8TimeTypes() {

        boolean localDateExists = true;
        boolean localDateTimeExists = true;

        try {
            Class.forName("org.neo4j.values.storable.DateValue", false, DatesEmbeddedTest.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            localDateExists = false;
        }

        try {
            Class.forName("org.neo4j.values.storable.LocalDateTimeValue", false,
                DatesEmbeddedTest.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            localDateTimeExists = false;
        }

        return localDateExists && localDateTimeExists;
    }
}
