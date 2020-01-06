/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

package org.neo4j.ogm.config;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author vince
 */
public class DriverServiceTest {

    private static final URI TMP_NEO4J_DB = Paths.get(System.getProperty("java.io.tmpdir"), "neo4j.db").toUri();

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

        Configuration driverConfiguration = new Configuration.Builder().uri("http://neo4j:password@localhost:7474")
            .build();

        SessionFactory sf = new SessionFactory(driverConfiguration, "org.neo4j.ogm.domain.social.User");
        Driver driver = sf.getDriver();
        assertThat(driver).isNotNull();
        sf.close();
    }

    @Test
    public void shouldLoadEmbeddedDriver() {
        Configuration driverConfiguration = new Configuration.Builder().uri(TMP_NEO4J_DB.toString()).build();

        SessionFactory sf = new SessionFactory(driverConfiguration, "org.neo4j.ogm.domain.social.User");
        Driver driver = sf.getDriver();
        assertThat(driver).isNotNull();
        driver.close();
        sf.close();
    }

    private static void deleteDirectory(File dir) {
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
        // now set the config to ignore SSL handshaking and try again;
        Configuration configuration = new Configuration.Builder().uri("https://neo4j:password@localhost:7473")
            .trustStrategy("ACCEPT_UNSIGNED").build();

        SessionFactory sf = new SessionFactory(configuration, "org.neo4j.ogm.domain.social.User");
        try (HttpDriver driver = (HttpDriver) sf.getDriver()) {
            driver.configure(configuration);
            driver.executeHttpRequest(request);
            Assert.fail("Should have thrown security exception");
        } catch (Exception e) {
            // expected
        }
        sf.close();

        sf = new SessionFactory(configuration, "org.neo4j.ogm.domain.social.User");
        try (HttpDriver driver = (HttpDriver) sf.getDriver()) {
            driver.configure(configuration);
            driver.executeHttpRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Should NOT have thrown security exception");
        }
        sf.close();
    }
}
