package org.neo4j.ogm.session.transaction;

import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.request.TransactionRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LongTransaction extends SimpleTransaction {

    private final Logger logger = LoggerFactory.getLogger(LongTransaction.class);

    private final TransactionRequestHandler transactionRequestHandler;

    public LongTransaction(MappingContext mappingContext, String url, TransactionRequestHandler transactionRequestHandler) {
        super(mappingContext, url);
        this.transactionRequestHandler = transactionRequestHandler;
    }

    public void commit() {
        transactionRequestHandler.commit(this);
        super.commit();
    }


    public void rollback() {
        transactionRequestHandler.rollback(this);
        super.rollback();
    }

    public void close() {
        transactionRequestHandler.rollback(this);
        super.close();
    }
}
