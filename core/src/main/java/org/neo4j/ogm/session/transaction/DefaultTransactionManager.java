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
package org.neo4j.ogm.session.transaction;

import static java.util.Collections.*;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.neo4j.ogm.exception.core.TransactionManagerException;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class DefaultTransactionManager implements TransactionManager {

    private final Session session;
    private final BiFunction<Transaction.Type, Iterable<String>, Transaction> transactionFactory;
    private final ThreadLocal<Transaction> currentThreadLocalTransaction = new ThreadLocal<>();

    public DefaultTransactionManager(Session session,
        Function<TransactionManager, BiFunction<Transaction.Type, Iterable<String>, Transaction>> transactionFactorySupplier) {
        this.session = session;
        this.transactionFactory = transactionFactorySupplier.apply(this);
    }

    /**
     * Opens a new transaction against a database instance.
     * Instantiation of the transaction is left to the driver
     *
     * @return a new {@link Transaction}
     */
    public Transaction openTransaction() {
        Transaction tx = currentThreadLocalTransaction.get();
        if (tx == null) {
            return openTransaction(Transaction.Type.READ_WRITE, emptySet());
        } else {
            return openTransaction(tx.type(), emptySet());
        }
    }

    /**
     * Opens a new transaction against a database instance.
     * Instantiation of the transaction is left to the driver
     *
     * @return a new {@link Transaction}
     */
    public Transaction openTransaction(Transaction.Type type, Iterable<String> bookmarks) {
        if (currentThreadLocalTransaction.get() == null) {
            currentThreadLocalTransaction.set(transactionFactory.apply(type, bookmarks));
        } else {
            ((AbstractTransaction) currentThreadLocalTransaction.get()).extend(type);
        }
        return currentThreadLocalTransaction.get();
    }

    /**
     * Rolls back the specified transaction.
     * The actual job of rolling back the transaction is left to the relevant driver. if
     * this is successful, the transaction is detached from this thread. Any new objects
     * are reset in the session, so that their ids are reset to null.
     * If the specified transaction is not the correct one for this thread, throws an exception
     *
     * @param transaction the transaction to rollback
     */
    public void rollback(Transaction transaction) {

        checkIfCurrentAndRemove(transaction, tx -> {
            List<Object> newlyRegisteredObjects = ((AbstractTransaction) tx).registeredNew();
            for (Object object : newlyRegisteredObjects) {
                ((Neo4jSession) session).context().reset(object);
            }
            newlyRegisteredObjects.clear();
        });
    }

    /**
     * Commits the specified transaction.
     * The actual job of committing the transaction is left to the relevant driver. if
     * this is successful, the transaction is detached from this thread.
     * If the specified transaction is not the correct one for this thread, throws an exception
     *
     * @param transaction the transaction to commit
     */
    public void commit(Transaction transaction) {

        checkIfCurrentAndRemove(transaction, tx -> {
            List<Object> newlyRegisteredObjects = tx.registeredNew();
            newlyRegisteredObjects.clear();
        });
    }

    private void checkIfCurrentAndRemove(Transaction transaction, Consumer<AbstractTransaction> action) {
        if (transaction != getCurrentTransaction()) {
            throw new TransactionManagerException("Transaction is not current for this thread");
        }

        if (transaction instanceof AbstractTransaction) {
            action.accept((AbstractTransaction) transaction);
        }

        currentThreadLocalTransaction.remove();
    }

    /**
     * Returns the current transaction for this thread, or null if none exists
     *
     * @return this thread's transaction
     */
    public Transaction getCurrentTransaction() {
        return currentThreadLocalTransaction.get();
    }

    public boolean canCommit() {

        AbstractTransaction tx = (AbstractTransaction) getCurrentTransaction();

        if (tx == null) {
            return false;
        }

        if (tx.extensions() == 0) {
            if (tx.status() == Transaction.Status.COMMIT_PENDING || tx.status() == Transaction.Status.OPEN
                || tx.status() == Transaction.Status.PENDING) {
                return true;
            }
        }
        return false;
    }

    public boolean canRollback() {

        AbstractTransaction tx = (AbstractTransaction) getCurrentTransaction();

        if (tx == null) {
            return false;
        }

        if (tx.extensions() == 0) {
            if (tx.status() == Transaction.Status.ROLLBACK_PENDING || tx.status() == Transaction.Status.COMMIT_PENDING
                || tx.status() == Transaction.Status.OPEN || tx.status() == Transaction.Status.PENDING) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void bookmark(String bookmark) {
        if (session != null) {
            session.withBookmark(bookmark);
        }
    }
}
