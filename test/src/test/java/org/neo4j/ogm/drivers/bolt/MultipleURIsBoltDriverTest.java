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
