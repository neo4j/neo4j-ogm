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

import java.util.Arrays;
import java.util.Properties;

import org.junit.Test;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 * @author Gerrit Meier
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
    public void shouldConfigureDatabase() {
        Configuration configuration;

        configuration = new Configuration.Builder().database(null).build();
        assertThat(configuration.getDatabase()).isNull();

        configuration = new Configuration.Builder().database("").build();
        assertThat(configuration.getDatabase()).isNull();

        configuration = new Configuration.Builder().database("  ").build();
        assertThat(configuration.getDatabase()).isNull();

        configuration = new Configuration.Builder().database("someDatabase").build();
        assertThat(configuration.getDatabase()).isEqualTo("someDatabase");
    }

    @Test
    public void shouldConfigureCredentialsFromURIWithUTF8Characters() {
        Configuration configuration = new Configuration.Builder()
            .uri("http://franti\u0161ek:Pass123@localhost:8080")
            .build();

        assertThat(configuration.getCredentials().credentials().toString()).isEqualTo("ZnJhbnRpxaFlazpQYXNzMTIz");
    }

    @Test(expected = RuntimeException.class)
    public void uriWithNoScheme() {
        Configuration configuration = new Configuration.Builder().uri("target/noe4j/my.db").build();
        fail("Should have thrown a runtime exception about a missing URI Scheme");
    }

    @Test
    public void shouldSetUsernameAndPasswordCredentialsForBoltProtocol() {
        String username = "neo4j";
        String password = "password";
        Configuration dbConfig = new Configuration.Builder().uri("bolt://" + username + ":" + password + "@localhost")
            .build();
        Credentials credentials = dbConfig.getCredentials();
        UsernamePasswordCredentials basic = (UsernamePasswordCredentials) credentials;
        assertThat(basic).isNotNull();
        assertThat(basic.getUsername()).isEqualTo(username);
        assertThat(basic.getPassword()).isEqualTo(password);
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

    @Test
    public void shouldDetectSimpleBoltSchemes() {

        for (String aSimpleScheme : Arrays.asList("bolt", "Bolt", "neo4j", "Neo4J")) {
            String uri = String.format("%s://localhost:7687", aSimpleScheme);
            Configuration configuration = new Configuration.Builder()
                .uri(uri)
                .build();

            assertThat(configuration.getDriverClassName()).isEqualTo("org.neo4j.ogm.drivers.bolt.driver.BoltDriver");
            assertThat(configuration.getURI()).isEqualTo(uri);
        }
    }

    @Test
    public void shouldDetectAdvancedBoltSchemes() {

        for (String anAdvancedScheme : Arrays.asList("bolt+s", "Bolt+ssc", "neo4j+s", "Neo4J+ssc")) {
            String uri = String.format("%s://localhost:7687", anAdvancedScheme);
            Configuration configuration = new Configuration.Builder()
                .uri(uri)
                .build();

            assertThat(configuration.getDriverClassName()).isEqualTo("org.neo4j.ogm.drivers.bolt.driver.BoltDriver");
            assertThat(configuration.getURI()).isEqualTo(uri);
        }
    }

    @Test
    public void shouldFailEarlyOnInvalidSchemes() {

        for (String invalidScheme : Arrays.asList("bolt+x", "neo4j+wth")) {
            String uri = String.format("%s://localhost:7687", invalidScheme);

            assertThatIllegalArgumentException()
                .isThrownBy(() -> new Configuration.Builder()
                    .uri(uri)
                    .build())
                .withMessage(
                    "A URI Scheme must be one of: bolt, bolt+routing, bolt+s, bolt+ssc, neo4j, neo4j+s, neo4j+ssc, file, http, https.");
        }
    }

    @Test
    public void shouldParseBaseBackagesWithEmtpyValue() {
        Properties properties = new Properties();
        properties.setProperty("base-packages", "");

        Configuration configuration = new Configuration.Builder(() -> properties).build();
        assertThat(configuration.getBasePackages())
            .isNotNull()
            .isEmpty();
    }

    @Test
    public void shouldParseBaseBackages() {
        Properties properties = new Properties();
        properties.setProperty("base-packages", "a  ,b,c ");

        Configuration configuration = new Configuration.Builder(() -> properties).build();
        assertThat(configuration.getBasePackages())
            .isNotNull()
            .containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    public void mergeBasePackagesShouldWorkWithNullBase() {
        Configuration configuration = new Configuration.Builder().build();

        String[] basePackages = configuration.mergeBasePackagesWith("a", "b");
        assertThat(basePackages).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    public void mergeBasePackagesShouldWorkWithEmptyAdditionalPackages() {
        Configuration configuration = new Configuration.Builder()
            .withBasePackages("a", "b")
            .build();

        String[] basePackages = configuration.mergeBasePackagesWith();
        assertThat(basePackages).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    public void mergeBasePackagesShouldWorkDealWithNulls() {
        Configuration configuration = new Configuration.Builder()
            .withBasePackages("a", null, "b")
            .build();

        String[] basePackages = configuration.mergeBasePackagesWith(null, "c");
        assertThat(basePackages).containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    public void mergeBasePackagesShouldRemoveDups() {
        Configuration configuration = new Configuration.Builder()
            .withBasePackages("a", "b")
            .build();

        String[] basePackages = configuration.mergeBasePackagesWith("A", "B", "a");
        assertThat(basePackages).containsExactlyInAnyOrder("A", "B", "a", "b");
    }

    @Test
    public void shouldDefaultToStrictQuerying() {
        Configuration.Builder builder = new Configuration.Builder();
        Configuration configuration = builder.build();
        assertThat(configuration.getUseStrictQuerying()).isTrue();
    }

    @Test
    public void changingQueryingModeShouldWork() {
        Configuration.Builder builder = new Configuration.Builder();
        Configuration configuration = builder.relaxedQuerying().build();
        assertThat(configuration.getUseStrictQuerying()).isFalse();
    }

    @Test
    public void shouldParseQUeryingMode() {

        Configuration configuration;

        configuration = new Configuration.Builder(() -> new Properties()).build();
        assertThat(configuration.getUseStrictQuerying()).isTrue();

        configuration = new Configuration.Builder(() -> {
            Properties properties = new Properties();
            properties.setProperty("use-strict-querying", "");
            return properties;
        }).build();
        assertThat(configuration.getUseStrictQuerying()).isTrue();

        configuration = new Configuration.Builder(() -> {
            Properties properties = new Properties();
            properties.setProperty("use-strict-querying", "true");
            return properties;
        }).build();
        assertThat(configuration.getUseStrictQuerying()).isTrue();

        configuration = new Configuration.Builder(() -> {
            Properties properties = new Properties();
            properties.setProperty("use-strict-querying", "false");
            return properties;
        }).build();
        assertThat(configuration.getUseStrictQuerying()).isFalse();
    }
}
