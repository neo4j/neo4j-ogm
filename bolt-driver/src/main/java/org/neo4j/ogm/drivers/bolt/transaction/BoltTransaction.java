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
            throw new CypherException(ce.code(), ce.getMessage(), ce);
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
