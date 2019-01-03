/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.Test;

/**
 * @author vince
 */
public class ConfigurationTest {

    @Test
    public void shouldConfigureProgrammatically() {
        Configuration.Builder builder = new Configuration.Builder();

        builder.autoIndex("assert");
        builder.generatedIndexesOutputDir("dir");
        builder.generatedIndexesOutputFilename("filename");
        builder.credentials("fred", "flintstone");
        builder.uri("http://localhost:8080");
        builder.connectionPoolSize(200);
        builder.encryptionLevel("REQUIRED");
        builder.trustStrategy("TRUST_SIGNED_CERTIFICATES");
        builder.trustCertFile("/tmp/cert");
        builder.connectionLivenessCheckTimeout(1000);

        Configuration configuration = builder.build();

        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.ASSERT);
        assertThat(configuration.getDumpDir()).isEqualTo("dir");
        assertThat(configuration.getDumpFilename()).isEqualTo("filename");
        assertThat(configuration.getDriverClassName()).isEqualTo("org.neo4j.ogm.drivers.http.driver.HttpDriver");
        assertThat(configuration.getCredentials().credentials().toString()).isEqualTo("ZnJlZDpmbGludHN0b25l");
        assertThat(configuration.getURI()).isEqualTo("http://localhost:8080");
        assertThat(configuration.getConnectionPoolSize()).isEqualTo(200);
        assertThat(configuration.getEncryptionLevel()).isEqualTo("REQUIRED");
        assertThat(configuration.getTrustStrategy()).isEqualTo("TRUST_SIGNED_CERTIFICATES");
        assertThat(configuration.getTrustCertFile()).isEqualTo("/tmp/cert");
        assertThat(configuration.getConnectionLivenessCheckTimeout().intValue()).isEqualTo(1000);
    }

    @Test
    public void shouldConfigureCredentialsFromURI() {
        Configuration configuration = new Configuration.Builder().uri("http://fred:flintstone@localhost:8080").build();
        // base 64 encoded credentials, e.g. use echo fred:flintstone | base64
        assertThat(configuration.getCredentials().credentials().toString()).isEqualTo("ZnJlZDpmbGludHN0b25l");
        assertThat(configuration.getURI()).isEqualTo("http://localhost:8080");
    }

    @Test
    public void shouldConfigureCredentialsFromURIWithUTF8Characters() {
        Configuration configuration = new Configuration.Builder()
            .uri("http://franti\u0161ek:Pass123@localhost:8080")
            .build();

        assertThat(configuration.getCredentials().credentials().toString()).isEqualTo("ZnJhbnRpxaFlazpQYXNzMTIz");
    }

    @Test
    public void shouldConfigureFromSimplePropertiesFile() {
        Configuration configuration = new Configuration.Builder(
            new ClasspathConfigurationSource("ogm-simple.properties")).build();

        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.NONE);
        assertThat(configuration.getDriverClassName()).isEqualTo("org.neo4j.ogm.drivers.http.driver.HttpDriver");
        assertThat(configuration.getCredentials().credentials().toString()).isEqualTo("bmVvNGo6cGFzc3dvcmQ=");
        assertThat(configuration.getURI()).isEqualTo("http://localhost:7474");
    }

    @Test
    public void shouldConfigureFromFilesystemPropertiesFilePath() throws Exception {
        File file = new File(getConfigFileAsRelativePath());
        FileConfigurationSource source = new FileConfigurationSource(file.getAbsolutePath());
        Configuration configuration = new Configuration.Builder(source).build();

        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.NONE);
        assertThat(configuration.getDriverClassName()).isEqualTo("org.neo4j.ogm.drivers.http.driver.HttpDriver");
        assertThat(configuration.getCredentials().credentials().toString()).isEqualTo("bmVvNGo6cGFzc3dvcmQ=");
        assertThat(configuration.getURI()).isEqualTo("http://localhost:7474");
    }

    @Test
    public void shouldConfigureFromFilesystemPropertiesFileURI() throws Exception {
        File file = new File(getConfigFileAsRelativePath());
        FileConfigurationSource source = new FileConfigurationSource(file.toURI().toString());
        Configuration configuration = new Configuration.Builder(source).build();

        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.NONE);
        assertThat(configuration.getDriverClassName()).isEqualTo("org.neo4j.ogm.drivers.http.driver.HttpDriver");
        assertThat(configuration.getCredentials().credentials().toString()).isEqualTo("bmVvNGo6cGFzc3dvcmQ=");
        assertThat(configuration.getURI()).isEqualTo("http://localhost:7474");
    }

    private String getConfigFileAsRelativePath() {
        try {
            // Copy ogm-simple to temp file - the file is only inside jar on TeamCity, we need regular file

            File tempFile = File.createTempFile("ogm-simple", ".properties");
            try (InputStream in = ConfigurationTest.class.getResourceAsStream("/ogm-simple.properties")) {
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                tempFile.deleteOnExit();
            }
            return tempFile.getPath();
        } catch (IOException e) {
            throw new RuntimeException("Could not create temp ogm-simple.properties", e);
        }
    }

    @Test(expected = RuntimeException.class)
    public void uriWithNoScheme() {
        Configuration configuration = new Configuration.Builder().uri("target/noe4j/my.db").build();
        fail("Should have thrown a runtime exception about a missing URI Scheme");
    }

    @Test
    public void shouldConfigureFromNameSpacePropertiesFile() {

        Configuration configuration = new Configuration.Builder(
            new ClasspathConfigurationSource("ogm-namespace.properties")).build();

        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.DUMP);
        assertThat(configuration.getDumpDir()).isEqualTo("hello");
        assertThat(configuration.getDumpFilename()).isEqualTo("generated-indexes2.cql");
        assertThat(configuration.getDriverClassName()).isEqualTo("org.neo4j.ogm.drivers.http.driver.HttpDriver");
        assertThat(configuration.getCredentials().credentials().toString()).isEqualTo("bmVvNGo6cGFzc3dvcmQ=");
        assertThat(configuration.getURI()).isEqualTo("http://localhost:7474");
        assertThat(configuration.getConnectionPoolSize()).isEqualTo(100);
        assertThat(configuration.getEncryptionLevel()).isEqualTo("NONE");
        assertThat(configuration.getTrustStrategy()).isEqualTo("TRUST_ON_FIRST_USE");
        assertThat(configuration.getTrustCertFile()).isEqualTo("/tmp/cert");
    }

    @Test
    public void shouldConfigureFromUsernamePasswordProperties() {

        Configuration configuration = new Configuration.Builder(
            new ClasspathConfigurationSource("ogm-password.properties")).build();

        assertThat(((UsernamePasswordCredentials) configuration.getCredentials()).getUsername()).isEqualTo("azerty");
        assertThat(((UsernamePasswordCredentials) configuration.getCredentials()).getPassword()).isEqualTo("uiop");
    }

    @Test
    public void shouldConfigureFromSpringBootPropertiesFile() {

        Configuration configuration = new Configuration.Builder(
            new ClasspathConfigurationSource("application.properties")).build();

        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.NONE);
        assertThat(configuration.getDriverClassName()).isEqualTo("org.neo4j.ogm.drivers.http.driver.HttpDriver");
        assertThat(configuration.getCredentials().credentials().toString()).isEqualTo("bmVvNGo6cGFzc3dvcmQ=");
        assertThat(configuration.getURI()).isEqualTo("http://localhost:7474");
    }

    @Test
    public void shouldParseBoltUriSchemesCaseInsensitive() {
        Configuration configuration = new Configuration.Builder()
            .uri("BOLT://localhost")
            .build();

        assertThat(configuration.getDriverClassName()).isEqualTo("org.neo4j.ogm.drivers.bolt.driver.BoltDriver");
        assertThat(configuration.getURI()).isEqualTo("BOLT://localhost");

        configuration = new Configuration.Builder()
            .uri("BOLT+ROUTING://localhost")
            .build();

        assertThat(configuration.getDriverClassName()).isEqualTo("org.neo4j.ogm.drivers.bolt.driver.BoltDriver");
        assertThat(configuration.getURI()).isEqualTo("BOLT+ROUTING://localhost");
    }

    @Test
    public void shouldParseHttpUriSchemesCaseInsensitive() {
        Configuration configuration = new Configuration.Builder()
            .uri("HTTP://localhost")
            .build();

        assertThat(configuration.getDriverClassName()).isEqualTo("org.neo4j.ogm.drivers.http.driver.HttpDriver");
        assertThat(configuration.getURI()).isEqualTo("HTTP://localhost");

        configuration = new Configuration.Builder()
            .uri("HTTPS://localhost")
            .build();

        assertThat(configuration.getDriverClassName()).isEqualTo("org.neo4j.ogm.drivers.http.driver.HttpDriver");
        assertThat(configuration.getURI()).isEqualTo("HTTPS://localhost");
    }

    @Test
    public void shouldParseEmbeddedUriSchemeCaseInsensitive() {
        Configuration configuration = new Configuration.Builder()
            .uri("FILE:///somewhere")
            .build();

        assertThat(configuration.getDriverClassName())
            .isEqualTo("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver");
        assertThat(configuration.getURI()).isEqualTo("FILE:///somewhere");
    }
}
