/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.session.transaction;

import org.neo4j.ogm.mapper.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 */
public class LongTransaction extends SimpleTransaction {

    private final Logger logger = LoggerFactory.getLogger(LongTransaction.class);

    private final TransactionManager transactionRequestHandler;

    public LongTransaction(MappingContext mappingContext, String url, TransactionManager transactionRequestHandler) {
        super(mappingContext, url);
        this.transactionRequestHandler = transactionRequestHandler;
    }

    public void commit() {
        super.commit();
        transactionRequestHandler.commit(this);
    }


    public void rollback() {
        super.rollback();
        transactionRequestHandler.rollback( this );
    }

    public void close() {
        super.close();
        if (this.status().equals(Status.OPEN) || this.status().equals(Status.PENDING)) {
            transactionRequestHandler.rollback(this);
        }
    }
}
