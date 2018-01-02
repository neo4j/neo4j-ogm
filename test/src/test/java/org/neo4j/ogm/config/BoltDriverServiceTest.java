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

package org.neo4j.ogm.config;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Frantisek Hartman
 */
public class BoltDriverServiceTest extends MultiDriverTestClass {

    @BeforeClass
    public static void setUp() throws Exception {
        assumeTrue(getBaseConfiguration().build().getDriverClassName().equals(BoltDriver.class.getName()));
    }

    @Test
    public void loadLoadBoltDriver() {
        String uri = getBaseConfiguration().build().getURI();
        Configuration driverConfiguration = new Configuration.Builder().uri(uri).build();
        SessionFactory sf = new SessionFactory(driverConfiguration, "org.neo4j.ogm.domain.social.User");
        Driver driver = sf.getDriver();
        assertThat(driver).isNotNull();
        sf.close();
    }

}
