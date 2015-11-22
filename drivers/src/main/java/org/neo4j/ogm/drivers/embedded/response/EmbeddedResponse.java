package org.neo4j.ogm.drivers.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public abstract class EmbeddedResponse<T> implements Response {

    protected final Result result;
    private final Transaction tx;
    private final Logger logger = LoggerFactory.getLogger(EmbeddedResponse.class);

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
        tx.close();
        logger.debug("Response closed and transaction {} committed", tx);

    }

    @Override
    public String[] columns() {
        return result.columns().toArray(new String[result.columns().size()]);
    }

}


