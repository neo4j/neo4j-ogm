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

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.exception.core.TransactionManagerException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public final class DefaultTransactionManager extends AbstractTransactionManager {

    private final ThreadLocal<Transaction> currentThreadLocalTransaction = new ThreadLocal<>();

    public DefaultTransactionManager(Driver driver, Session session) {
        super(driver, session);
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
    protected Transaction openOrExtend(Supplier<Transaction> opener, UnaryOperator<Transaction> extender) {
        var current = getCurrentTransaction();
        if (current == null) {
            var newTransaction = opener.get();
            currentThreadLocalTransaction.set(newTransaction);
            return newTransaction;
        } else {
            return extender.apply(current);
        }
    }

    @Override
    protected void removeIfCurrent(Transaction transaction, Runnable action) {
        if (transaction != getCurrentTransaction()) {
            throw new TransactionManagerException("Transaction is not current for this thread");
        }

        action.run();
        currentThreadLocalTransaction.remove();
    }
}
