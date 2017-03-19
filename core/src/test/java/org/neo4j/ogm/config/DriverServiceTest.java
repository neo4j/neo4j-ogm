/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.*;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;

/**
 * @author vince
 */
public class DriverServiceTest {

    public static final String TMP_NEO4J_DB = Paths.get(System.getProperty("java.io.tmpdir"), "neo4j.db").toString();
    private Configuration driverConfiguration = new Configuration();

    @BeforeClass
    public static void createEmbeddedStore() throws IOException {
        try {
            Files.createDirectory(Paths.get(TMP_NEO4J_DB));
        } catch (FileAlreadyExistsException e) {
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

        Driver driver = Components.loadDriver(driverConfiguration);
        assertNotNull(driver);
        driver.close();
    }

    @Test
    public void shouldLoadEmbeddedDriver() {
        driverConfiguration.setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver");

        String uri = "file://" + TMP_NEO4J_DB;

        driverConfiguration.setURI(uri);

        Driver driver = Components.loadDriver(driverConfiguration);
        assertNotNull(driver);
        driver.close();
    }

    @Test
    public void loadLoadBoltDriver() {
        driverConfiguration.setDriverClassName("org.neo4j.ogm.drivers.bolt.driver.BoltDriver");
        driverConfiguration.setURI("bolt://neo4j:password@localhost");
        Driver driver = Components.loadDriver(driverConfiguration);
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


    /**
     * This test is marked @Ignore by default because it requires a locally running
     * Neo4j server to be installed, authenticating with 'neo4j:password'.
     * Note: The mechanism to ignore SSL handshaking installs a trust-everybody trust manager into
     * any HttpDriver that is created with the 'ACCEPT_UNSIGNED' trust strategy baseConfiguration setting.
     * It does not contaminate the behaviour of other any HttpDrivers that may be running.
     */
    @Test
    @Ignore
    public void shouldDisableCertificateValidationIfIgnoreSSLHandshake() {

        HttpPost request = new HttpPost("https://neo4j:password@localhost:7473/db/data/transaction/commit");
        request.setEntity(new StringEntity("{\n" +
                "  \"statements\" : [ {\n" +
                "    \"statement\" : \"MATCH (n) RETURN id(n)\"\n" +
                "  } ]\n" +
                "}", "UTF-8"));

        // note that the default driver class is set from the URI if a driver class has not yet been configured
        Configuration configuration = new Configuration();
        configuration.setURI("https://neo4j:password@localhost:7473");

        try (HttpDriver driver = (HttpDriver) Components.loadDriver(configuration)) {
            driver.executeHttpRequest(request);
            Assert.fail("Should have thrown security exception");
        } catch (Exception e) {
            // expected
        }

        // now set the config to ignore SSL handshaking and try again;
        configuration.setTrustStrategy("ACCEPT_UNSIGNED");

        try (HttpDriver driver = (HttpDriver) Components.loadDriver(configuration)) {
            driver.executeHttpRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Should NOT have thrown security exception");
        }
    }
}
