package org.neo4j.ogm.session.transaction;

import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.mapper.MappingContext;

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
    private int status = 0;

    public Transaction(MappingContext mappingContext, String url) {
        this.mappingContext = mappingContext;
        this.url = url;
        this.autocommit = url.endsWith("/commit");
        this.contexts = new ArrayList<>();
        status = OPEN;
    }

    final void append(CypherContext context) {
        contexts.add(context);
        status = OPEN & PENDING;
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

    public final void commit() {
        // 1. commit the transaction

        // 2. iterate over the cypher contexts and update the mapping context accordingly.
        // 3. clear the tx history
        contexts.clear();
        status = OPEN | COMMITTED;
    }

    public int status() {
        return status;
    }
}
