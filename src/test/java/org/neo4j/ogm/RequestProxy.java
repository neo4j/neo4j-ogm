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

package org.neo4j.ogm;

import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.config.DriverConfig;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;

/**
 * @author Vince Bickers
 */
public abstract class RequestProxy implements Driver<String> {

    protected abstract String[] getResponse();

    public Neo4jResponse<String> execute(String request, Transaction tx) {
        return new Response(getResponse());
    }

    static class Response implements Neo4jResponse<String> {

        private final String[] jsonModel;
        private int count = 0;

        public Response(String[] jsonModel) {
            this.jsonModel = jsonModel;
        }

        public String next()  {
            if (count < jsonModel.length) {
                String json = jsonModel[count];
                count++;
                return json;
            }
            return null;
        }

        @Override
        public void close() {
            // nothing to do.
        }

        @Override
        public void initialiseScan(ResponseRecord record) {
            // nothing to do
        }

        @Override
        public String[] columns() {
            return new String[0];
        }

        @Override
        public int rowId() {
            return count-1;
        }
    }




    @Override
    public void rollback(Transaction tx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void commit(Transaction tx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(DriverConfig config) {

    }

    @Override
    public Transaction openTransaction(MappingContext context, TransactionManager tx, boolean autoCommit) {
        return null;
    }
}
