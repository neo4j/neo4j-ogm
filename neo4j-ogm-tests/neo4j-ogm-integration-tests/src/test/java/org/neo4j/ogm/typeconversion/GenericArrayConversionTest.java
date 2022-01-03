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
import static org.junit.Assume.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.internal.util.ServerVersion;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.ogm.domain.gh492.BaseUser.ByteUser;
import org.neo4j.ogm.domain.gh492.BaseUser.IntUser;
import org.neo4j.ogm.domain.gh492.BaseUser.IntegerUser;
import org.neo4j.ogm.domain.gh492.BaseUser.LongUser;
import org.neo4j.ogm.domain.gh492.BaseUser.StringUser;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Michael J. Simons
 */
public class GenericArrayConversionTest {
    protected static final String DOMAIN_PACKAGE = "org.neo4j.ogm.domain.gh492";

    protected static Neo4j serverControls;

    protected static SessionFactory sessionFactory;

    protected static boolean supportsBytePacking = false;

    @BeforeClass
    public static void startServer() {

        serverControls = Neo4jBuilders.newInProcessBuilder().build();

        Driver driver = GraphDatabase.driver(serverControls.boltURI(), Config.builder().withoutEncryption().build());

        supportsBytePacking = ServerVersion.version(driver).greaterThanOrEqual(ServerVersion.v3_4_0);
        sessionFactory = new SessionFactory(new BoltDriver(driver), DOMAIN_PACKAGE);
    }

    @Test // GH-492
    public void byteSampleTest() {

        assumeTrue(supportsBytePacking);

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
