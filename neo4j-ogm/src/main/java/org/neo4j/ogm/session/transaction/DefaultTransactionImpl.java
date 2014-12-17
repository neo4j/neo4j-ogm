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
        if (status == Status.OPEN) {
            contexts.add(context);
            status = Status.PENDING;
            if (autocommit) {
                commit();
            }
        } else {
            throw new RuntimeException("Transaction is closed. Cannot accept new operations");
        }
    }

    public final String url() {
        return url;
    }

    // rollback a transaction that has pending writes
    // calling rollback on a transaction with no pending read/writes is an error
    public final void rollback() {
        if (status == Status.PENDING) {
            contexts.clear();
            status = Status.ROLLEDBACK;
        } else {
            throw new RuntimeException("Transaction has no pending operations. Cannot rollback");
        }
    }

    // commit a transaction that has pending writes
    // calling commit on a transaction with no pending read/writes is an error
    public final void commit() {

        if (status == Status.PENDING ) {

            // 1. iterate over the cypher contexts and update the mapping context accordingly.
            for (CypherContext cypherContext : contexts) {
                // todo : subclass these
                for (Object o : cypherContext.log())  {
                    if (o instanceof MappedRelationship) {
                        mappingContext.remember((MappedRelationship) o);
                    } else {
                        mappingContext.remember(o);
                    }
                }
            }

            // 2. clear the tx history
            logger.debug("clearing transaction log");
            contexts.clear();
            status = Status.COMMITTED;
        } else {
            throw new RuntimeException("Transaction has no pending operations. Cannot commit");
        }

    }

    public Status status() {
        return status;
    }

}
