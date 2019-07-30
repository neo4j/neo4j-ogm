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
package org.neo4j.ogm.drivers.bolt.response;

import java.util.Set;

import org.neo4j.driver.Record;
import org.neo4j.driver.StatementResult;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luanne Misquitta
 */
public abstract class BoltResponse<T> implements Response {

    final StatementResult result;

    private final Logger LOGGER = LoggerFactory.getLogger(BoltResponse.class);

    BoltResponse(StatementResult result) {
        this.result = result;
    }

    @Override
    public T next() {
        try {
            return fetchNext();
        } catch (ClientException ce) {
            LOGGER.debug("Error executing Cypher: {}, {}", ce.code(), ce.getMessage());
            throw new CypherException(ce.code(), ce.getMessage(), ce);
        }
    }

    protected abstract T fetchNext();

    @Override
    public void close() {
        // Consume the rest of the result and thus closing underlying resources.
        result.consume();
    }

    @Override
    public String[] columns() {
        if (result.hasNext()) {
            Record record = result.peek();
            if (record != null) {
                Set<String> columns = result.peek().asMap().keySet();
                return columns.toArray(new String[columns.size()]);
            }
        }
        return new String[0];
    }
}
