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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collections;

import org.assertj.core.util.Files;
import org.junit.BeforeClass;
import org.neo4j.driver.v1.Values;
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

    @BeforeClass
    public static void init() {

        File temporaryFolder = Files.newTemporaryFolder();
        temporaryFolder.deleteOnExit();

        Configuration ogmConfiguration = new Configuration.Builder()
            .uri("file://" + temporaryFolder.getAbsolutePath())
            .useNativeTypes()
            .build();

        EmbeddedDriver driver = new EmbeddedDriver();
        driver.configure(ogmConfiguration);
        sessionFactory = new SessionFactory(driver, SpatialEmbeddedTest.class.getPackage().getName());
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

        assertThat(loaded.getTemporalAmount()).isEqualTo(DurationValue.parse("P13Y370M45DT25H120M"));
    }
}
