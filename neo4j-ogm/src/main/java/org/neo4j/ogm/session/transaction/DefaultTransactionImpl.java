package org.neo4j.ogm.session.transaction;

import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.mapper.MappedRelationship;
import org.neo4j.ogm.mapper.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DefaultTransactionImpl implements Transaction {

    private final Logger logger = LoggerFactory.getLogger(DefaultTransactionImpl.class);
    private final MappingContext mappingContext;
    private final String url;
    private final boolean autocommit;

    private final List<CypherContext> contexts;

    private Status status = Status.OPEN;

    public DefaultTransactionImpl(MappingContext mappingContext, String url) {
        this.mappingContext = mappingContext;
        this.url = url;
        this.autocommit = url.endsWith("/commit");
        this.contexts = new ArrayList<>();
    }

    public final void append(CypherContext context) {
        logger.debug("Appending transaction context " + context);
        if (status == Status.OPEN) {
            contexts.add(context);
            status = Status.PENDING;
            if (autocommit) {
                commit();
            }
        } else {
            throw new TransactionException("Transaction is closed. Cannot accept new operations");
        }
    }

    public final String url() {
        return url;
    }

    public final void rollback() {
        logger.debug("Attempting to rollback transaction");
        if (status == Status.PENDING) {
            contexts.clear();
            status = Status.ROLLEDBACK;
        } else {
            throw new TransactionException("Transaction has no pending operations. Cannot rollback");
        }
    }

    public final void commit() {
        logger.debug("Attempting to commit transaction");
        if (status == Status.PENDING ) {
            for (CypherContext cypherContext : contexts) {
                logger.debug("Synchronizing transaction context " + cypherContext + " with session context");
                // todo : subclass these
                for (Object o : cypherContext.log())  {
                    if (o instanceof MappedRelationship) {
                        mappingContext.remember((MappedRelationship) o);
                    } else {
                        mappingContext.remember(o);
                    }
                }
            }
            contexts.clear();
            status = Status.COMMITTED;
        } else {
            throw new TransactionException("Transaction has no pending operations. Cannot commit");
        }
    }

    public Status status() {
        return status;
    }

}