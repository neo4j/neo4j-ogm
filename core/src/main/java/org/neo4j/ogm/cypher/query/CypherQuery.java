/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.cypher.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.neo4j.ogm.request.OptimisticLockingConfig;
import org.neo4j.ogm.request.Statement;

/**
 * Simple encapsulation of a Cypher query and its parameters
 * Note, this object will be transformed directly to JSON so don't add anything here that is
 * not part of the HTTP Transactional endpoint syntax
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class CypherQuery implements Statement {

    protected String statement;
    protected Map<String, Object> parameters = new HashMap<>();

    /**
     * Constructs a new {@link CypherQuery} based on the given Cypher query string and query parameters.
     *
     * @param cypher     The parameterised Cypher query string
     * @param parameters The name-value pairs that satisfy the parameters in the given query
     */
    public CypherQuery(String cypher, Map<String, ?> parameters) {
        this.statement = cypher;
        this.parameters.putAll(parameters);
    }

    public String getStatement() {
        return this.statement;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public String[] getResultDataContents() {
        return new String[0];
    }

    @Override
    public boolean isIncludeStats() {
        return false;
    }

    // ** By default queries/statements are not checked for results, only DefaultRowModelRequest is

    @Override
    public Optional<OptimisticLockingConfig> optimisticLockingConfig() {
        return Optional.empty();
    }
}
