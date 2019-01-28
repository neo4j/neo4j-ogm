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
package org.neo4j.ogm.drivers.bolt;

import static org.assertj.core.api.Assertions.*;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Frantisek Hartman
 */
public class MultipleURIsBoltDriverTest {

    @Test
    public void throwCorrectExceptionOnUnavailableCluster() throws Exception {

        Configuration configuration = new Configuration.Builder()
            .uri("bolt+routing://localhost:1022")
            .uris(new String[] { "bolt+routing://localhost:1023" })
            .verifyConnection(true)
            .build();

        try {
            new SessionFactory(configuration, "org.neo4j.ogm.domain.social");
        } catch (Exception e) {
            Throwable cause = e.getCause();
            assertThat(cause).isInstanceOf(ServiceUnavailableException.class);
            assertThat(cause).hasMessage("Failed to discover an available server");
        }
    }

    @Test
    @Ignore("this needs local causal cluster running")
    public void connectToCCUsingConfiguration() throws Exception {
        Configuration configuration = new Configuration.Builder()
            .uri("bolt+routing://localhost:1023")
            .uris(new String[] { "bolt+routing://localhost:7688",
                "bolt+routing://localhost:7687",
                "bolt+routing://localhost:7689" })
            .verifyConnection(true)
            .build();

        new SessionFactory(configuration, "org.neo4j.ogm.domain.social");
    }

    @Test
    @Ignore("this needs local causal cluster running")
    public void connectToCCUsingProperties() throws Exception {
        Configuration configuration = new Configuration.Builder(
            new ClasspathConfigurationSource("ogm-bolt-uris.properties"))
            .build();

        new SessionFactory(configuration, "org.neo4j.ogm.domain.social");
    }
}
