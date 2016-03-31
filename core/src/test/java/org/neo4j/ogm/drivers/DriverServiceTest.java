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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.service.DriverService;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;

/**
 * @author vince
 */
public class DriverServiceTest {

    public static final String TMP_NEO4J_DB = Paths.get(System.getProperty("java.io.tmpdir"), "neo4j.db").toString();
    private DriverConfiguration driverConfiguration = Components.configuration().driverConfiguration();

    @BeforeClass
    public static void createEmbeddedStore() throws IOException {
        try {
            Files.createDirectory(Paths.get(TMP_NEO4J_DB));
        } catch(FileAlreadyExistsException e){
            // the directory already exists.
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void deleteEmbeddedStore() throws IOException {
        deleteDirectory(new File(TMP_NEO4J_DB));
    }

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

        String uri = "file://" + TMP_NEO4J_DB;

        driverConfiguration.setURI(uri);

        Driver driver = DriverService.load(driverConfiguration);
        assertNotNull(driver);
        driver.close();
    }

    @Test
    public void loadLoadBoltDriver() {
        driverConfiguration.setDriverClassName("org.neo4j.ogm.drivers.bolt.driver.BoltDriver");
        driverConfiguration.setURI("bolt://neo4j:password@localhost");
        Driver driver = DriverService.load(driverConfiguration);
        assertNotNull(driver);
        driver.close();
    }

    static void deleteDirectory(File dir) throws IOException {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                deleteDirectory(file);
            }
        }
        if (!dir.delete()) {
            throw new RuntimeException("Failed to delete file: " + dir);
        }
    }


}
