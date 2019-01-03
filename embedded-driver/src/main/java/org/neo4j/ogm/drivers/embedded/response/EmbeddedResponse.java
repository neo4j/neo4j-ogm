/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.drivers.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public abstract class EmbeddedResponse<T> implements Response {

    protected final Result result;
    private final Logger logger = LoggerFactory.getLogger(EmbeddedResponse.class);
    private final TransactionManager transactionManager;

    public EmbeddedResponse(Result result, TransactionManager transactionManager) {
        logger.debug("Response opened: {}", this);
        this.transactionManager = transactionManager;
        this.result = result;
    }

    @Override
    public abstract T next();

    @Override
    public void close() {

        // if there is no current transaction available, the response is already closed.
        // it is not an error to call close() multiple times, and in certain circumstances
        // it may be unavoidable.
        if (transactionManager.getCurrentTransaction() != null) {
            // release the response resource
            result.close();
            logger.debug("Response closed: {}", this);
        }
    }

    @Override
    public String[] columns() {
        return result.columns().toArray(new String[result.columns().size()]);
    }
}


