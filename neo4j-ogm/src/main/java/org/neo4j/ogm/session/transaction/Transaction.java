package org.neo4j.ogm.session.transaction;

import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.mapper.MappedRelationship;
import org.neo4j.ogm.mapper.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Transaction {

    private final Logger logger = LoggerFactory.getLogger(Transaction.class);

    public final static int OPEN        = 1;
    public final static int PENDING     = 2;
    public final static int ROLLEDBACK  = 4;
    public final static int COMMITTED   = 8;

    private final MappingContext mappingContext;
    private final String url;
    private final boolean autocommit;

    private final List<CypherContext> contexts;

    private int status = 0;

    public Transaction(MappingContext mappingContext, String url) {
        this.mappingContext = mappingContext;
        this.url = url;
        this.autocommit = url.endsWith("/commit");
        this.contexts = new ArrayList<>();
        status = OPEN;
    }

    public final void append(CypherContext context) {
        if (status == OPEN) {
            contexts.add(context);
            status = PENDING;
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
        if (status == PENDING) {
            contexts.clear();
            status = ROLLEDBACK;
        } else {
            throw new RuntimeException("Transaction has no pending operations. Cannot rollback");
        }
    }

    // commit a transaction that has pending writes
    // calling commit on a transaction with no pending read/writes is an error
    public final void commit() {

        if (status == PENDING ) {

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
            status = COMMITTED;
        } else {
            throw new RuntimeException("Transaction has no pending operations. Cannot commit");
        }

    }

    public int status() {
        return status;
    }

}
