package org.neo4j.ogm.api.transaction;


import org.neo4j.ogm.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public abstract class AbstractTransaction implements Transaction {

    private final Logger logger = LoggerFactory.getLogger(Transaction.class);

    protected final TransactionManager transactionManager;
    protected Transaction.Status status = Transaction.Status.OPEN;

    public AbstractTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void rollback() {
        logger.debug("rollback invoked");
        if (status == Status.OPEN || status == Status.PENDING) {
            if (transactionManager != null) {
                transactionManager.rollback(this);
            }
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
            rollback();
        }
        status = Status.CLOSED;
    }


}
