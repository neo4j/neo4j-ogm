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

package neo4j.ogm.drivers.bolt.driver;

import neo4j.ogm.drivers.bolt.request.BoltRequest;
import neo4j.ogm.drivers.bolt.transaction.BoltTransaction;
import org.neo4j.driver.Session;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author vince
 */
public class BoltDriver extends AbstractConfigurableDriver
{

    private Session boltSession;

    // required for service loader mechanism
    public BoltDriver() {
    }

    public BoltDriver(DriverConfiguration driverConfiguration) {
        configure(driverConfiguration);
    }

    @Override
    public Transaction newTransaction() {
        return new BoltTransaction(transactionManager, boltSession);
    }

    @Override
    public void close() {
        // cannot detect if boltSession is already closed, so explicitly set to null.
        if (boltSession != null) {
            boltSession.close();
            boltSession = null;
        }
    }

    @Override
    public Request request() {
        return new BoltRequest(boltSession);
    }

}