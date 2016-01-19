/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.drivers;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.service.DriverService;

import static org.junit.Assert.assertNotNull;

/**
 * @author vince
 */
public class DriverServiceTest {

    private DriverConfiguration driverConfiguration = new DriverConfiguration();

    @Test
    public void shouldLoadHttpDriver() {

        driverConfiguration.setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver");
        driverConfiguration.setURI("http://neo4j:password@localhost:7474");

        Driver driver = DriverService.load(driverConfiguration);
        assertNotNull(driver);
        driver.close();
    }

    @Test
    public void shouldLoadEmbeddedDriver() {
        driverConfiguration.setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver");
        driverConfiguration.setURI("file:///var/tmp/neo4j.db");

        Driver driver = DriverService.load(driverConfiguration);
        assertNotNull(driver);
        driver.close();
    }

    @Test
    @Ignore // until we can switch to 3.0 in memory
    public void loadLoadBoltDriver() {
        driverConfiguration.setDriverClassName("org.neo4j.ogm.drivers.bolt.driver.BoltDriver");
        driverConfiguration.setURI("bolt://neo4j:password@localhost");
        Driver driver = DriverService.load(driverConfiguration);
        assertNotNull(driver);
        driver.close();
    }
}
