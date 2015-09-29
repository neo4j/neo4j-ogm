package org.neo4j.ogm.driver.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.session.response.Response;

/**
 * @author vince
 */
public abstract class EmbeddedResponse<T> implements Response {

    protected final Result result;
    private final Transaction tx;


    public EmbeddedResponse(Transaction tx, Result result) {
        this.result = result;
        this.tx = tx;
    }

    @Override
    public abstract T next();

    @Override
    public void close() {
        result.close();
        tx.success();
    }

    @Override
    public String[] columns() {
        return result.columns().toArray(new String[result.columns().size()]);
    }

}


