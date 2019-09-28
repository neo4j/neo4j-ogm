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
package org.neo4j.ogm.drivers.bolt.driver;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(GraphDatabase.class)
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*" })
public class BoltDriverConfigurationTest {

    @Before
    public void setup() {
        PowerMockito.mockStatic(GraphDatabase.class);
        Mockito
            .when(GraphDatabase.routingDriver(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenAnswer(Answers.RETURNS_MOCKS);
        Mockito
            .when(GraphDatabase.driver(Mockito.any(URI.class), Mockito.any(), Mockito.any()))
            .thenAnswer(Answers.RETURNS_MOCKS);
    }

    @Test
    public void shouldSupportAdditionalRoutingUris() {

        String uri = "neo4j://somewhere1";
        String[] uris = { "neo4j://somewhere2", "neo4j://somewhere3" };
        List<URI> routingUris = Arrays.asList(
            URI.create("neo4j://somewhere1"),
            URI.create("neo4j://somewhere2"),
            URI.create("neo4j://somewhere3"));

        Configuration configuration = new Configuration.Builder()
            .uri(uri)
            .uris(uris)
            .verifyConnection(true)
            .build();

        // trigger bolt driver creation
        new SessionFactory(configuration, "non.existing.package");

        assertThat(configuration.getURI()).isEqualTo(uri);
        assertThat(configuration.getURIS()).isEqualTo(uris);

        PowerMockito.verifyStatic(GraphDatabase.class);
        GraphDatabase.routingDriver(Mockito.eq(routingUris), Mockito.any(), Mockito.any());
    }

    @Test
    public void shouldRenameBoltRoutingSchemeToNeo4j() {

        String uri = "bolt+routing://somewhere1";
        String[] uris = { "bolt+routing://somewhere2", "bolt+routing://somewhere3" };
        List<URI> routingUris = Arrays.asList(
            URI.create("neo4j://somewhere1"),
            URI.create("neo4j://somewhere2"),
            URI.create("neo4j://somewhere3"));

        Configuration configuration = new Configuration.Builder()
            .uri(uri)
            .uris(uris)
            .verifyConnection(true)
            .build();

        // trigger bolt driver creation
        new SessionFactory(configuration, "non.existing.package");

        assertThat(configuration.getURI()).isEqualTo(uri);
        assertThat(configuration.getURIS()).isEqualTo(uris);

        PowerMockito.verifyStatic(GraphDatabase.class);
        GraphDatabase.routingDriver(Mockito.eq(routingUris), Mockito.any(), Mockito.any());
    }

    @Test
    public void shouldSupportAdditionalRoutingUrisWithoutDefiningUri() {

        String[] uris = { "neo4j://somewhere1", "neo4j://somewhere2", "neo4j://somewhere3" };
        List<URI> routingUris = Arrays.asList(
            URI.create("neo4j://somewhere1"),
            URI.create("neo4j://somewhere2"),
            URI.create("neo4j://somewhere3"));

        Configuration configuration = new Configuration.Builder()
            .uris(uris)
            .verifyConnection(true)
            .build();

        // trigger bolt driver creation
        new SessionFactory(configuration, "non.existing.package");

        assertThat(configuration.getURIS()).isEqualTo(uris);

        PowerMockito.verifyStatic(GraphDatabase.class);
        GraphDatabase.routingDriver(Mockito.eq(routingUris), Mockito.any(), Mockito.any());
    }

    @Test
    public void shouldCallDefaultGraphDatabaseInstantiationWithJustOneUriInUris() {

        String[] uris = { "neo4j://somewhere1" };
        URI uri = URI.create("neo4j://somewhere1");

        Configuration configuration = new Configuration.Builder()
            .uris(uris)
            .verifyConnection(true)
            .build();

        // trigger bolt driver creation
        new SessionFactory(configuration, "non.existing.package");

        assertThat(configuration.getURIS()).isEqualTo(uris);

        PowerMockito.verifyStatic(GraphDatabase.class);
        GraphDatabase.driver(Mockito.eq(uri), Mockito.any(), Mockito.any());
    }

    @Test
    public void shouldCallDefaultGraphDatabaseInstantiationWithOneUri() {

        String uriValue ="neo4j://somewhere1";
        URI uri = URI.create(uriValue);

        Configuration configuration = new Configuration.Builder()
            .uri(uriValue)
            .verifyConnection(true)
            .build();

        // trigger bolt driver creation
        new SessionFactory(configuration, "non.existing.package");

        assertThat(configuration.getURI()).isEqualTo(uriValue);

        PowerMockito.verifyStatic(GraphDatabase.class);
        GraphDatabase.driver(Mockito.eq(uri), Mockito.any(), Mockito.any());
    }

    @Test
    public void boltDriverJavaDriverInitializationShouldFailIfNoUriOrUrisAreSupplied() {

        assertThatThrownBy(() -> new BoltDriver().configure(new Configuration.Builder().verifyConnection(true).build()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("You must provide either an URI or at least one URI in the URIS parameter.");
    }

}
