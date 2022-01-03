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
package org.neo4j.ogm.config;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.Test;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 * @author Gerrit Meier
 */
public class ConfigurationFromPropertiesFileTest {

    @Test
    public void shouldConfigureFromSimplePropertiesFile() {
        Configuration configuration = new Configuration.Builder(
            new ClasspathConfigurationSource("ogm-simple.properties")).build();

        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.NONE);
        assertThat(configuration.getDriverClassName()).isEqualTo("org.neo4j.ogm.drivers.http.driver.HttpDriver");
        assertThat(configuration.getCredentials().credentials().toString()).isEqualTo("bmVvNGo6cGFzc3dvcmQ=");
        assertThat(configuration.getURI()).isEqualTo("http://localhost:7474");
        assertThat(configuration.getBasePackages()).isEqualTo(new String[] { "org.neo4j.ogm.domain.bike" });
        assertThat(configuration.getUseNativeTypes()).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void shouldConfigureFromFilesystemPropertiesFilePath() {
        File file = new File(getConfigFileAsRelativePath());
        FileConfigurationSource source = new FileConfigurationSource(file.getAbsolutePath());
        Configuration configuration = new Configuration.Builder(source).build();

        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.NONE);
        assertThat(configuration.getDriverClassName()).isEqualTo("org.neo4j.ogm.drivers.http.driver.HttpDriver");
        assertThat(configuration.getCredentials().credentials().toString()).isEqualTo("bmVvNGo6cGFzc3dvcmQ=");
        assertThat(configuration.getURI()).isEqualTo("http://localhost:7474");
    }

    @Test
    public void shouldConfigureFromFilesystemPropertiesFileURI() {
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
    public void shouldLoadHttpDriverConfigFromPropertiesFile() {
        Configuration driverConfig = new Configuration.Builder(
            new ClasspathConfigurationSource("http.driver.properties")).build();
        assertThat(driverConfig.getURI()).isEqualTo("http://localhost:7474");
    }

    @Test
    public void shouldLoadEmbeddedDriverConfigFromPropertiesFile() {
        Configuration driverConfig = new Configuration.Builder(
            new ClasspathConfigurationSource("embedded.driver.properties")).build();
        assertThat(driverConfig.getURI()).isEqualTo("file:///var/tmp/neo4j.db");
    }

    @Test
    public void shouldLoadBoltDriverConfigFromPropertiesFile() {
        Configuration driverConfig = new Configuration.Builder(
            new ClasspathConfigurationSource("bolt.driver.properties")).build();
        assertThat(driverConfig.getURI()).isEqualTo("bolt://localhost");
        assertThat(driverConfig.getConnectionPoolSize()).isEqualTo(150);
        assertThat(driverConfig.getEncryptionLevel()).isEqualTo("NONE");
        assertThat(driverConfig.getTrustStrategy()).isEqualTo("TRUST_ON_FIRST_USE");
        assertThat(driverConfig.getTrustCertFile()).isEqualTo("/tmp/cert");
    }
}
