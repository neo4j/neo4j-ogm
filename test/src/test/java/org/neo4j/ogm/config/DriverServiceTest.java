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

import java.net.URI;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.*;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.DriverManager;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;

/**
 * @author vince
 */
public class DriverServiceTest {

    public static final URI TMP_NEO4J_DB = Paths.get(System.getProperty("java.io.tmpdir"), "neo4j.db").toUri();
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

        driverConfiguration.setURI("http://neo4j:password@localhost:7474");

        DriverManager.register(driverConfiguration.getDriverClassName());
        Driver driver = DriverManager.getDriver();
        assertNotNull(driver);
        driver.close();
        DriverManager.degregister(driver);
    }

    @Test
    public void shouldLoadEmbeddedDriver() {
        driverConfiguration.setURI(TMP_NEO4J_DB.toString());

        DriverManager.register(driverConfiguration.getDriverClassName());
        Driver driver = DriverManager.getDriver();
        assertNotNull(driver);
        driver.close();
        DriverManager.degregister(driver);

    }

    @Test
    public void loadLoadBoltDriver() {
        driverConfiguration.setURI("bolt://neo4j:password@localhost");
        DriverManager.register(driverConfiguration.getDriverClassName());
        Driver driver = DriverManager.getDriver();
        assertNotNull(driver);
        driver.close();
        DriverManager.degregister(driver);
    }

    static void deleteDirectory(File dir)  {
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

        DriverManager.register(driverConfiguration.getDriverClassName());
        try (HttpDriver driver = (HttpDriver) DriverManager.getDriver()) {
            driver.configure(driverConfiguration);
            driver.executeHttpRequest(request);
            Assert.fail("Should have thrown security exception");
        } catch (Exception e) {
            // expected
        }

        // now set the config to ignore SSL handshaking and try again;
        configuration.setTrustStrategy("ACCEPT_UNSIGNED");

        DriverManager.register(driverConfiguration.getDriverClassName());
        try (HttpDriver driver = (HttpDriver) DriverManager.getDriver()) {
            driver.configure(driverConfiguration);
            driver.executeHttpRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Should NOT have thrown security exception");
        }
    }
}
