package org.neo4j.ogm.session.transaction;

import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.Session;

import java.util.ArrayList;
import java.util.List;

public class Transaction {

    public final static int OPEN        = 1;
    public final static int PENDING     = 2;
    public final static int ROLLEDBACK  = 4;
    public final static int COMMITTED   = 8;

    private final MappingContext mappingContext;
    private final String url;
    private final boolean autocommit;

    private final List<CypherContext> contexts;
    private final Session session;

    private int status = 0;

    public Transaction(MappingContext mappingContext, String url, Session session) {
        this.mappingContext = mappingContext;
        this.url = url;
        this.autocommit = url.endsWith("/commit");
        this.session = session;
        this.contexts = new ArrayList<>();
        status = OPEN;
    }

    public final void append(CypherContext context) {
        contexts.add(context);
        status = OPEN | PENDING;
        if (autocommit) {
            commit();
        }
    }

    public final String url() {
        return url;
    }

    public final void rollback() {
        contexts.clear();
        status = OPEN | ROLLEDBACK;
    }

    // commit a transaction that has pending writes
    // calling commit on a transaction with no pending read/writes has no effect
    public final void commit() {

        if (status == (OPEN | PENDING) ) {

            session.flush();

            // 1. iterate over the cypher contexts and update the mapping context accordingly.
            for (CypherContext cypherContext : contexts) {
                // mappingContext.remember(cypherContext.dirtyObjects[])
                // mappingContext.remember(new relationships);
                // mappingContext.remove(deleted relationships);
            }

            // 2. clear the tx history
            contexts.clear();
        }
        status = OPEN | COMMITTED;
    }

    public int status() {
        return status;
    }


}
