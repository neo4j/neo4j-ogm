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

import org.junit.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.service.Components;

import static org.junit.Assert.*;

/**
 * @author vince
 */

public class DriverConfigTest {

    @Test
    public void shouldLoadHttpDriverConfigFromPropertiesFile() {
        DriverConfiguration driverConfig = new DriverConfiguration(new Configuration("http.driver.properties"));
        assertEquals("http://neo4j:password@localhost:7474", driverConfig.getURI());
    }

    @Test
    public void shouldLoadEmbeddedDriverConfigFromPropertiesFile() {
        DriverConfiguration driverConfig = new DriverConfiguration(new Configuration("embedded.driver.properties"));
        assertEquals("file:///var/tmp/neo4j.db", driverConfig.getURI());
    }

    @Test
    public void shouldLoadBoltDriverConfigFromPropertiesFile() {
        DriverConfiguration driverConfig = new DriverConfiguration(new Configuration("bolt.driver.properties"));
        assertEquals("bolt://neo4j:password@localhost", driverConfig.getURI());
    }

    @Test
    public void shouldUseTemporaryEphemeralFileStoreForEmbeddedDriverIfNoURISpecified() {

        Configuration configuration = new Configuration();
        DriverConfiguration driverConfiguration = new DriverConfiguration(configuration);
        driverConfiguration.setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver");
        Components.configure(configuration);
        EmbeddedDriver driver = new EmbeddedDriver(driverConfiguration);

        assertNotNull(driverConfiguration.getURI());
        assertTrue(driverConfiguration.getURI().contains("/neo4j.db"));

        driver.close();

    }

}
