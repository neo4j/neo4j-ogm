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

package org.neo4j.ogm.driver;

import java.net.URI;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 *
 * The AbstractConfigurableDriver is used by all drivers to configure themselves.
 *
 * The configure method takes a generic {@link Configuration} object, which is used to configure the
 * driver appropriately. This object contains of one or more key-value
 * pairs. Every driver configuration must contain a mandatory key "URI", whose corresponding value is
 * a text representation of the driver uri, for example:
 *
 * setConfig("URI", "http://username:password@hostname:port")
 *
 * if credentials are not present the URI, they can be specified in one of
 * two ways, either as a plain text username/password key-values pair in the configuration e.g.
 *
 * setConfig("username", "bilbo")
 * setConfig("password", "hobbit")
 *
 * or, alternatively using the "credentials" key
 *
 * setConfig("credentials", new UsernamePasswordCredentials("bilbo", "hobbit")
 *
 * @author vince
 */
public abstract class AbstractConfigurableDriver implements Driver {

    protected DriverConfiguration driverConfig;
    protected TransactionManager transactionManager;

    @Override
    public void configure(DriverConfiguration config) {
        this.driverConfig = config;
        setCredentials();
    }

    @Override
    public DriverConfiguration getConfiguration() {
        assert(driverConfig != null);
        return driverConfig;
    }

    @Override
    public void setTransactionManager(TransactionManager transactionManager) {
        assert(transactionManager != null);
        this.transactionManager = transactionManager;
    }

    private void setCredentials() {
        if (driverConfig.getCredentials() == null && driverConfig.getURI() != null) {
            try {
                URI uri = new URI(driverConfig.getURI());
                String authInfo = uri.getUserInfo();
                if (authInfo != null) {
                    String[] parts = uri.getUserInfo().split(":");
                    driverConfig.setCredentials(parts[0], parts[1]);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
