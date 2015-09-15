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
import org.neo4j.ogm.mapper.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class TransactionManager {

    private final Logger logger = LoggerFactory.getLogger(TransactionManager.class);
    private final Driver driver;
    private final String transactionEndpoint;
    private final Neo4jCredentials credentials;

    private static final ThreadLocal<Transaction> transaction = new ThreadLocal<>();

    public TransactionManager(Driver driver, String server) {

        this.transactionEndpoint = transactionEndpoint(server);
        this.driver = driver;
        this.credentials = CredentialsService.userNameAndPassword();

        this.driver.authorize(credentials);
        transaction.remove();
    }


    public TransactionManager(Driver driver, String server, Neo4jCredentials credentials) {

        this.transactionEndpoint = transactionEndpoint(server);
        this.driver = driver;
        this.credentials = credentials;

        this.driver.authorize(credentials);
        transaction.remove();
    }

    public Transaction openTransaction(MappingContext mappingContext) {
        String transactionEndpoint = driver.newTransactionUrl(this.transactionEndpoint);
        logger.debug("Creating new transaction with endpoint " + transactionEndpoint);
        transaction.set(new LongTransaction(mappingContext, transactionEndpoint, this));
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

    private String transactionEndpoint(String server) {
        if (server == null) {
            return server;
        }
        String url = server;

        if (!server.endsWith("/")) {
            url += "/";
        }
        return url + "db/data/transaction";
    }

}
