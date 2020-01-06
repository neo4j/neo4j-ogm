/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 */
abstract class EmbeddedResponse<T> implements Response {

    private final Logger logger = LoggerFactory.getLogger(EmbeddedResponse.class);
    protected final Result result;

    public EmbeddedResponse(Result result) {
        logger.debug("Response opened: {}", this);
        this.result = result;
    }

    @Override
    public abstract T next();

    @Override
    public void close() {

        // release the response resource, this might closed implicit transaction in the GraphDatabaseService.
        result.close();
        logger.debug("Response closed: {}", this);
    }

    @Override
    public String[] columns() {
        return result.columns().toArray(new String[result.columns().size()]);
    }
}


