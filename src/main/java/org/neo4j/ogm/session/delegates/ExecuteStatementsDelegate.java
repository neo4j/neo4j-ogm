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
package org.neo4j.ogm.session.delegates;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.neo4j.ogm.cypher.query.RowModelQueryWithStatistics;
import org.neo4j.ogm.session.Capability;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.result.QueryStatistics;
import org.neo4j.ogm.session.result.RowQueryStatisticsResult;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class ExecuteStatementsDelegate implements Capability.ExecuteStatements {

    private final Neo4jSession session;

    public ExecuteStatementsDelegate(Neo4jSession neo4jSession) {
        this.session = neo4jSession;
    }

    @Override
    public QueryStatistics execute(String cypher, Map<String, Object> parameters) {
        if (StringUtils.isEmpty(cypher)) {
            throw new RuntimeException("Supplied cypher statement must not be null or empty.");
        }

        if (parameters == null) {
            throw new RuntimeException("Supplied Parameters cannot be null.");
        }
        assertNothingReturned(cypher);
        String url  = session.ensureTransaction().url();
        // NOTE: No need to check if domain objects are parameters and flatten them to json as this is done
        // for us using the existing execute() method.
        RowModelQueryWithStatistics parameterisedStatement = new RowModelQueryWithStatistics(cypher, parameters);
        try (Neo4jResponse<RowQueryStatisticsResult> response = session.requestHandler().execute(parameterisedStatement, url)) {
            RowQueryStatisticsResult result = response.next();
            return result == null ? null : result.getStats();
        }
    }

    @Override
    public QueryStatistics execute(String statement) {
        if (StringUtils.isEmpty(statement)) {
            throw new RuntimeException("Supplied cypher statement must not be null or empty.");
        }
        assertNothingReturned(statement);
        RowModelQueryWithStatistics parameterisedStatement = new RowModelQueryWithStatistics(statement, Utils.map());
        String url = session.ensureTransaction().url();
        try (Neo4jResponse<RowQueryStatisticsResult> response = session.requestHandler().execute(parameterisedStatement, url)) {
            RowQueryStatisticsResult result = response.next();
            return result == null ? null : result.getStats();
        }
    }


    private void assertNothingReturned(String cypher) {
        if (cypher.toUpperCase().contains(" RETURN ")) {
            throw new RuntimeException("execute() must not return data. Use query() instead.");
        }
    }

}
