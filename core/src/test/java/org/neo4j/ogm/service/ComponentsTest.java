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

package org.neo4j.ogm.service;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;

import static org.junit.Assert.assertNotNull;

/**
 * @author vince
 */
public class ComponentsTest
{
    @Test
    public void shouldGetDriver() {
        assertNotNull( Components.driver() );
    }

    @Test
    public void shouldGetCompiler() {
        assertNotNull(Components.compiler());
    }

    @Test
    public void shouldCustomiseHttpDriverClient() {

        if (Components.driver() instanceof HttpDriver) {

            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal( 1 );
            connectionManager.setDefaultMaxPerRoute( 1 );

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .build();

            Driver driver = new HttpDriver( httpClient );

            Components.setDriver(driver);

            Assert.assertEquals(driver, Components.driver());

        }
    }
}
