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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.neo4j.ogm.cypher.query.GraphModelQuery;
import org.neo4j.ogm.cypher.query.Query;
import org.neo4j.ogm.cypher.query.RowModelQuery;
import org.neo4j.ogm.cypher.query.RowModelQueryWithStatistics;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.session.*;
import org.neo4j.ogm.session.request.strategy.AggregateStatements;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.result.QueryResult;
import org.neo4j.ogm.session.result.Result;
import org.neo4j.ogm.session.result.RowModel;
import org.neo4j.ogm.session.result.RowQueryStatisticsResult;

/**
 * @author Vince Bickers
 */
public class ExecuteQueriesDelegate implements Capability.ExecuteQueries {

    private static final Pattern WRITE_CYPHER_KEYWORDS = Pattern.compile("\\b(CREATE|MERGE|SET|DELETE|REMOVE)\\b");
    
    private final Neo4jSession session;

    public ExecuteQueriesDelegate(Neo4jSession neo4jSession) {
        this.session = neo4jSession;
    }

    @Override
    public <T> T queryForObject(Class<T> type, String cypher, Map<String, ?> parameters) {
        Iterable<T> results = query(type, cypher, parameters);

        int resultSize = Utils.size(results);

        if (resultSize < 1 ) {
            return null;
        }

        if (resultSize > 1) {
            throw new RuntimeException("Result not of expected size. Expected 1 row but found " + resultSize);
        }

        return results.iterator().next();
    }

    @Override
    public Result query(String cypher, Map<String, ?> parameters) {
        return query(cypher, parameters, isReadOnly(cypher));
    }

    @Override
    public <T> Iterable<T> query(Class<T> type, String cypher, Map<String, ?> parameters) {
        validateQuery(cypher, parameters, false); //we'll allow modifying statements
        if (type == null || type.equals(Void.class)) {
            throw new RuntimeException("Supplied type must not be null or void.");
        }
        return executeAndMap(type, cypher, parameters, new EntityRowModelMapper<T>());
    }

    @Override
    public Result query(String cypher, Map<String, ?> parameters, boolean readOnly) {
        validateQuery(cypher, parameters, readOnly);

        //If readOnly=true, just execute the query. If false, execute the query and return stats as well
        if(readOnly) {
            return new QueryResult(executeAndMap(null, cypher, parameters, new MapRowModelMapper()),null);
        }
        else {
            String url  = session.ensureTransaction().url();
            RowModelQueryWithStatistics parameterisedStatement = new RowModelQueryWithStatistics(cypher, parameters);
            try (Neo4jResponse<RowQueryStatisticsResult> response = session.requestHandler().execute(parameterisedStatement, url)) {
                RowQueryStatisticsResult result = response.next();
                RowModelMapper rowModelMapper = new MapRowModelMapper();
                Collection rowResult = new LinkedHashSet();
                for (Iterator<Object> iterator = result.getRows().iterator(); iterator.hasNext(); ) {
                    List next =  (List) iterator.next();
                    rowModelMapper.mapIntoResult(rowResult, next.toArray(), response.columns());
                }
                return new QueryResult(rowResult, result.getStats());

            }
        }

    }

    private <T> Iterable<T> executeAndMap(Class<T> type, String cypher, Map<String, ?> parameters, RowModelMapper<T> rowModelMapper) {
        if (StringUtils.isEmpty(cypher)) {
            throw new RuntimeException("Supplied cypher statement must not be null or empty.");
        }

        if (parameters == null) {
            throw new RuntimeException("Supplied Parameters cannot be null.");
        }

        String url = session.ensureTransaction().url();

        if (type != null && session.metaData().classInfo(type.getSimpleName()) != null) {
            Query qry = new GraphModelQuery(cypher, parameters);
            try (Neo4jResponse<GraphModel> response = session.requestHandler().execute(qry, url)) {
                return session.responseHandler().loadAll(type, response);
            }
        } else {
            RowModelQuery qry = new RowModelQuery(cypher, parameters);
            try (Neo4jResponse<RowModel> response = session.requestHandler().execute(qry, url)) {

                String[] variables = response.columns();

                Collection<T> result = new ArrayList<>();
                RowModel rowModel;
                while ((rowModel = response.next()) != null) {
                    rowModelMapper.mapIntoResult(result, rowModel.getValues(), variables);
                }

                return result;
            }
        }
    }

    @Override
    public long countEntitiesOfType(Class<?> entity) {
        ClassInfo classInfo = session.metaData().classInfo(entity.getName());
        if (classInfo == null) {
            return 0;
        }

        RowModelQuery countStatement = new AggregateStatements().countNodesLabelledWith(classInfo.labels());
        String url  = session.ensureTransaction().url();
        try (Neo4jResponse<RowModel> response = session.requestHandler().execute(countStatement, url)) {
            RowModel queryResult = response.next();
            return queryResult == null ? 0 : ((Number) queryResult.getValues()[0]).longValue();
        }
    }

    private boolean isReadOnly(String cypher) {
        Matcher matcher = WRITE_CYPHER_KEYWORDS.matcher(cypher.toUpperCase());
        return !matcher.find();
    }

    private void validateQuery(String cypher, Map<String, ?> parameters, boolean readOnly) {
        if(readOnly && !isReadOnly(cypher)) {
            throw new RuntimeException("Cypher query must not modify the graph if readOnly=true");
        }

        if (StringUtils.isEmpty(cypher)) {
            throw new RuntimeException("Supplied cypher statement must not be null or empty.");
        }

        if (parameters == null) {
            throw new RuntimeException("Supplied Parameters cannot be null.");
        }
    }

}
