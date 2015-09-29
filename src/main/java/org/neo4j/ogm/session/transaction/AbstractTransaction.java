package org.neo4j.ogm.session.transaction;

import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vince
 */
public abstract class AbstractTransaction implements Transaction {

    private final Logger logger = LoggerFactory.getLogger(Transaction.class);

    protected final List<CypherContext> contexts;
    protected final TransactionManager transactionManager;

    protected Transaction.Status status = Transaction.Status.OPEN;

    public AbstractTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.contexts = new ArrayList<>();
    }

    public final void append(CypherContext context) {

        logger.debug("Appending transaction context " + context);

        if (status == Transaction.Status.OPEN || status == Transaction.Status.PENDING) {
            contexts.add(context);
            status = Transaction.Status.PENDING;

            if (transactionManager == null || transactionManager.getCurrentTransaction() == null) {
                commit();
            }

        } else {
            throw new TransactionException("Transaction is no longer open. Cannot accept new operations");
        }
    }

    public void rollback() {
        logger.debug("rollback invoked");
        if (status == Status.OPEN || status == Status.PENDING) {
            if (transactionManager != null) {
                transactionManager.rollback(this);
            }
            contexts.clear();
            status = Status.ROLLEDBACK;
        } else {
            throw new TransactionException("Transaction is no longer open. Cannot rollback");
        }
    }

    public void commit() {
        logger.debug("commit invoked");
        if (status == Status.OPEN || status == Status.PENDING) {
            if (transactionManager != null) {
                transactionManager.commit(this);

            }
            status = Status.COMMITTED;
        } else {
            throw new TransactionException("Transaction is no longer open. Cannot commit");
        }
    }

    public final Status status() {
        return status;
    }

    public void close() {
        if (status == Status.PENDING || status == Status.OPEN) {
            commit();
        }
        status = Status.CLOSED;
    }


}
