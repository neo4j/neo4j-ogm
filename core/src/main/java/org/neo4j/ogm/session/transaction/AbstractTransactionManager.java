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

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * This abstract base class of a Neo4j-OGM {@link org.neo4j.ogm.transaction.TransactionManager} can be used to adapt
 * the handling of thread local transactions to various scenarios that might not be able to deal with Thread locals or
 * that are keen on not having them for performance reasons.
 * <p>
 * The implemented methods of the interface are intentionally final so that they are guaranteed to work occoring to the
 * systems' specification.
 *
 * @author Michael J. Simons
 * @since 4.0.3
 */
public abstract class AbstractTransactionManager implements TransactionManager {

    private final Session session;
    private final BiFunction<Transaction.Type, Iterable<String>, Transaction> transactionFactory;

    public AbstractTransactionManager(Session session,
        Function<TransactionManager, BiFunction<Transaction.Type, Iterable<String>, Transaction>> transactionFactorySupplier) {
        this.session = session;
        this.transactionFactory = transactionFactorySupplier.apply(this);
    }

    @Override
    public final void bookmark(String bookmark) {
        if (session != null) {
            session.withBookmark(bookmark);
        }
    }

    /**
     * Opens a new transaction against a database instance.
     * Instantiation of the transaction is left to the driver
     *
     * @return a new {@link Transaction}
     */
    @Override
    public final Transaction openTransaction() {
        return openTransaction(null, Set.of());
    }

    /**
     * Opens a new transaction against a database instance.
     * Instantiation of the transaction is left to the driver
     *
     * @return a new {@link Transaction}
     */
    @Override
    public final Transaction openTransaction(Transaction.Type type, Iterable<String> bookmarks) {
        return openOrExtend(
            () -> transactionFactory.apply(type == null ? Transaction.Type.READ_WRITE : type, bookmarks),
            transaction -> {
                if (!(transaction instanceof AbstractTransaction abstractTransaction)) {
                    throw new IllegalStateException(
                        "There's already an ongoing transaction for the current thread and it is not extendable.");
                }

                abstractTransaction.extend(type == null ? abstractTransaction.type() : type);
                return abstractTransaction;
            }
        );
    }

    /**
     * Opens a new transaction if there is no current via the {@code opener} or passes a current transaction to the
     * {@coode extender}. The latter will try to extend the transaction if it's compatible.
     *
     * @param opener   The opener of new transaction
     * @param extender The extender trying to create sub transactions
     * @return The new current transaction
     */
    protected abstract Transaction openOrExtend(Supplier<Transaction> opener, UnaryOperator<Transaction> extender);

    @Override
    public final void close(Transaction transaction, Consumer<TransactionClosedListener> callback) {

        removeIfCurrent(transaction, () -> {
            callback.accept((status, entities) -> {
                if (status == Transaction.Status.ROLLEDBACK) {
                    var mappingContext = ((Neo4jSession) session).context();
                    entities.forEach(mappingContext::reset);
                }
            });
        });
    }

    /**
     * Check if {@code transaction} is the transaction belonging to this thread, if so, calls the action
     * and then detach the transaction from the thread.
     *
     * @param action The action to be run prior to detaching / unlocking the transaction from the thread.
     */
    protected abstract void removeIfCurrent(Transaction transaction, Runnable action);
}
