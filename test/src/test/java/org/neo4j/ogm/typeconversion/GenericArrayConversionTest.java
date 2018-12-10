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
package org.neo4j.ogm.typeconversion;

import static org.assertj.core.api.Assertions.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.ogm.domain.gh492.BaseUser.ByteUser;
import org.neo4j.ogm.domain.gh492.BaseUser.IntUser;
import org.neo4j.ogm.domain.gh492.BaseUser.IntegerUser;
import org.neo4j.ogm.domain.gh492.BaseUser.LongUser;
import org.neo4j.ogm.domain.gh492.BaseUser.StringUser;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Michael J. Simons
 */
public class GenericArrayConversionTest {
    protected static final String DOMAIN_PACKAGE = "org.neo4j.ogm.domain.gh492";

    protected static ServerControls serverControls;

    protected static SessionFactory sessionFactory;

    @BeforeClass
    public static void startServer() {
        serverControls = TestServerBuilders.newInProcessBuilder().newServer();
        Driver driver = new BoltDriver(
            GraphDatabase.driver(serverControls.boltURI(), Config.build().withoutEncryption().toConfig()));
        sessionFactory = new SessionFactory(driver, DOMAIN_PACKAGE);
    }

    @Test // GH-492
    public void byteSampleTest() {
        ByteUser byteUser = new ByteUser();
        byteUser.setLoginName("test-byteUser");
        byteUser.setGenericValue(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 });
        byteUser.setNotGenericValue(new byte[] { 7, 6, 5, 4, 3, 2, 1, 0 });

        Session session = sessionFactory.openSession();
        session.save(byteUser);
        session.clear();

        byteUser = session.load(ByteUser.class, byteUser.getId());

        assertThat(byteUser.getNotGenericValue()).isInstanceOf(byte[].class);
        assertThat(byteUser.getGenericValue()).isInstanceOf(byte[].class);
    }

    @Test // GH-492
    public void intSampleTest() {
        IntUser intUser = new IntUser();
        intUser.setLoginName("test-intUser");
        intUser.setGenericValue(new int[] { 0, 1, 2, 3, 4, 5, 6, 7 });

        Session session = sessionFactory.openSession();
        session.save(intUser);
        session.clear();

        intUser = session.load(IntUser.class, intUser.getId());
        assertThat(intUser.getGenericValue()).isInstanceOf(int[].class);
    }

    @Test // GH-492
    public void integerSampleTest() {
        IntegerUser integerUser = new IntegerUser();
        integerUser.setLoginName("test-intUser");
        integerUser.setGenericValue(new Integer[] { Integer.MIN_VALUE, Integer.MAX_VALUE });

        Session session = sessionFactory.openSession();
        session.save(integerUser);
        session.clear();

        integerUser = session.load(IntegerUser.class, integerUser.getId());
        assertThat(integerUser.getGenericValue()).isInstanceOf(Integer[].class)
            .containsExactly(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Test // GH-492
    public void longSampleTest() {
        LongUser longUser = new LongUser();
        longUser.setLoginName("test-intUser");
        longUser.setGenericValue(new long[] { Long.MIN_VALUE, Long.MAX_VALUE });

        Session session = sessionFactory.openSession();
        session.save(longUser);
        session.clear();

        longUser = session.load(LongUser.class, longUser.getId());
        assertThat(longUser.getGenericValue()).isInstanceOf(long[].class)
            .containsExactly(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Test // GH-492
    public void stringSampleTest() {
        StringUser stringUser = new StringUser();
        stringUser.setLoginName("test-stringUser");
        stringUser.setGenericValue(new String[] { "1", "2", "3", "4", "5", "6", "7" });

        Session session = sessionFactory.openSession();
        session.save(stringUser);
        session.clear();

        stringUser = session.load(StringUser.class, stringUser.getId());
        assertThat(stringUser.getGenericValue()).isInstanceOf(String[].class);
    }

    @AfterClass
    public static void stopServer() {
        sessionFactory.close();
        serverControls.close();
    }
}
