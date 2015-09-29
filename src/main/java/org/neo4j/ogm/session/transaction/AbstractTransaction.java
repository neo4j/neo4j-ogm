package org.neo4j.ogm.session.transaction;

import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.mapper.MappedRelationship;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.TransientRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vince
 */
public abstract class AbstractTransaction implements Transaction {

    private final Logger logger = LoggerFactory.getLogger(Transaction.class);
    private final MappingContext mappingContext;
    private final List<CypherContext> contexts;
    private final boolean autoCommit;
    private final TransactionManager transactionManager;

    protected Transaction.Status status = Transaction.Status.OPEN;

    public AbstractTransaction(MappingContext mappingContext, TransactionManager transactionManager, boolean autoCommit) {
        this.mappingContext = mappingContext;
        this.transactionManager = transactionManager;
        this.autoCommit = autoCommit;
        this.contexts = new ArrayList<>();
    }

    public final void append(CypherContext context) {
        logger.debug("Appending transaction context " + context);
        if (status == Transaction.Status.OPEN || status == Transaction.Status.PENDING) {
            contexts.add(context);
            status = Transaction.Status.PENDING;
            if (autoCommit()) {
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
            synchroniseSession();
            status = Status.COMMITTED;
        } else {
            throw new TransactionException("Transaction is no longer open. Cannot commit");
        }
    }

    public final Status status() {
        return status;
    }

    public void close() {
        status = Status.CLOSED;
    }

    private void synchroniseSession()  {

        for (CypherContext cypherContext : contexts) {

            logger.debug("Synchronizing transaction context " + cypherContext + " with session context");

            for (Object o : cypherContext.log())  {
                logger.debug("checking cypher context object: " + o);
                if (o instanceof MappedRelationship) {
                    MappedRelationship mappedRelationship = (MappedRelationship) o;
                    if (mappedRelationship.isActive()) {
                        logger.debug("activating (${})-[:{}]->(${})", mappedRelationship.getStartNodeId(), mappedRelationship.getRelationshipType(), mappedRelationship.getEndNodeId());
                        mappingContext.registerRelationship((MappedRelationship) o);
                    } else {
                        logger.debug("de-activating (${})-[:{}]->(${})", mappedRelationship.getStartNodeId(), mappedRelationship.getRelationshipType(), mappedRelationship.getEndNodeId());
                        mappingContext.mappedRelationships().remove(mappedRelationship);
                    }
                } else if (!(o instanceof TransientRelationship)) {
                    logger.debug("remembering " + o);
                    mappingContext.remember(o);
                }
            }
            logger.debug("number of objects: " + cypherContext.log().size());
        }

        contexts.clear();
    }

    public boolean autoCommit() {
        return autoCommit;
    }


}
