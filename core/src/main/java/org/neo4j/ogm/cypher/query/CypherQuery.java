/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.cypher.query;

import java.util.HashMap;
import java.util.Map;

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

}
