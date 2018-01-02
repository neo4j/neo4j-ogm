/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.drivers.bolt.transaction;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.exception.TransactionException;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public class BoltTransaction extends AbstractTransaction {

    private static final String NEO_CLIENT_ERROR_SECURITY = "Neo.ClientError.Security";
    private final Transaction nativeTransaction;
    private final Session nativeSession;
    private final Logger LOGGER = LoggerFactory.getLogger(BoltTransaction.class);

    public BoltTransaction(TransactionManager transactionManager, Transaction transaction, Session session, Type type) {
        super(transactionManager);
        this.nativeTransaction = transaction;
        this.nativeSession = session;
        this.type = type;
    }

    @Override
    public void rollback() {
        try {
            if (transactionManager.canRollback()) {
                LOGGER.debug("Rolling back native transaction: {}", nativeTransaction);
                if (nativeTransaction.isOpen()) {
                    nativeTransaction.failure();
                    nativeTransaction.close();
                } else {
                    LOGGER.warn("Transaction is already closed");
                }
                closeNativeSessionIfPossible();
            }
        } catch (Exception e) {
            closeNativeSessionIfPossible();
            throw new TransactionException(e.getLocalizedMessage(), e);
        } finally {
            super.rollback();
        }
    }

    @Override
    public void commit() {
        final boolean canCommit = transactionManager.canCommit();
        try {
            if (canCommit) {
                LOGGER.debug("Committing native transaction: {}", nativeTransaction);
                if (nativeTransaction.isOpen()) {
                    nativeTransaction.success();
                    nativeTransaction.close();
                    nativeSession.close();
                } else {
                    throw new IllegalStateException("Transaction is already closed");
                }
            }
        } catch (ClientException ce) {
            closeNativeSessionIfPossible();
            if (ce.code().startsWith(NEO_CLIENT_ERROR_SECURITY)) {
                throw new ConnectionException("Security Error: " + ce.code() + ", " + ce.getMessage(), ce);
            }
            throw new CypherException("Error executing Cypher", ce, ce.code(), ce.getMessage());
        } catch (Exception e) {
            closeNativeSessionIfPossible();
            throw new TransactionException(e.getLocalizedMessage(), e);
        } finally {
            super.commit();
            if (canCommit) {
                transactionManager.bookmark(nativeSession.lastBookmark());
            }
        }
    }

    public Transaction nativeBoltTransaction() {
        return nativeTransaction;
    }

    private void closeNativeSessionIfPossible() {
        if (nativeSession.isOpen()) {
            nativeSession.close();
        }
    }
}
