/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.session.transaction;

import org.neo4j.ogm.authentication.CredentialsService;
import org.neo4j.ogm.authentication.Neo4jCredentials;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.config.DriverConfig;
import org.neo4j.ogm.mapper.MappingContext;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class TransactionManager {

    private final Driver driver;
    private final Neo4jCredentials credentials;

    private static final ThreadLocal<Transaction> transaction = new ThreadLocal<>();

    public TransactionManager(Driver driver, String server) {
        this(driver, server, CredentialsService.userNameAndPassword());
    }

    public TransactionManager(Driver driver, String server, Neo4jCredentials credentials) {

        this.credentials = credentials;
        this.driver = driver;

        // todo: hack to get this working for now
        DriverConfig driverConfig = new DriverConfig();
        driverConfig.setConfig("server", server);

        this.driver.authorize(credentials);
        this.driver.configure(driverConfig);

        transaction.remove();
    }

    /**
     * Opens a new transaction against a database instance.
     *
     * Instantiation of the transaction is left to the driver
     *
     * @param mappingContext
     * @return
     */
    public Transaction openTransaction(MappingContext mappingContext) {
        transaction.set(driver.openTransaction(mappingContext, this));
        return transaction.get();
    }

    public void rollback(Transaction tx) {
        driver.rollback(tx);
        transaction.remove();
    }

    public void commit(Transaction tx) {
        driver.commit(tx);
        transaction.remove();
    }

    public Transaction getCurrentTransaction() {
        return transaction.get();
    }

    public Neo4jCredentials credentials() {
        return credentials;
    }


}
