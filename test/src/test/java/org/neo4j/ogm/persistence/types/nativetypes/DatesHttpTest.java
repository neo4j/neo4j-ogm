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

import java.net.URI;
import java.util.Collections;

import org.junit.BeforeClass;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Values;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public class DatesHttpTest extends DatesTestBase {

    @BeforeClass
    public static void init() {

        Configuration ogmConfiguration = new Configuration.Builder()
            .uri(TestServerBuilders.newInProcessBuilder().newServer().httpURI().toString())
            .encryptionLevel(Config.EncryptionLevel.NONE.name())
       //     .useNativeTypes()
            .build();

        HttpDriver driver = new HttpDriver();
        driver.configure(ogmConfiguration);
        sessionFactory = new SessionFactory(driver, DatesHttpTest.class.getPackage().getName());
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
}
