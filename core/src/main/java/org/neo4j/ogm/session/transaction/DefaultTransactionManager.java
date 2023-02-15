/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
     * Returns the current transaction for this thread, or null if none exists
     *
     * @return this thread's transaction
     */
    public Transaction getCurrentTransaction() {
        return currentThreadLocalTransaction.get();
    }

    @Override
    public void close(Transaction transaction, Consumer<NewObjectNotifier> callback) {

        if (transaction != getCurrentTransaction()) {
            throw new TransactionManagerException("Transaction is not current for this thread");
        }

        callback.accept((status, o) -> {
            if (status == Transaction.Status.ROLLEDBACK) {
                ((Neo4jSession) session).context().reset(o);
            }
        });

        currentThreadLocalTransaction.remove();
    }

    @Override
    public void bookmark(String bookmark) {
        if (session != null) {
            session.withBookmark(bookmark);
        }
    }
}
