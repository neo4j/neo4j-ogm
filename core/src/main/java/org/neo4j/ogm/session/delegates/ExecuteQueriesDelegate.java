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

import org.apache.commons.lang.StringUtils;
import org.neo4j.ogm.model.*;
import org.neo4j.ogm.request.GraphModelRequest;
import org.neo4j.ogm.request.RowModelStatisticsRequest;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.cypher.query.AbstractRequest;
import org.neo4j.ogm.cypher.query.DefaultGraphModelRequest;
import org.neo4j.ogm.cypher.query.DefaultRowModelRequest;
import org.neo4j.ogm.cypher.query.DefaultRowModelStatisticsRequest;
import org.neo4j.ogm.mapper.EntityRowModelMapper;
import org.neo4j.ogm.mapper.MapRowModelMapper;
import org.neo4j.ogm.mapper.RowModelMapper;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.session.Capability;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.session.request.strategy.AggregateStatements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public QueryStatistics query(String cypher, Map<String, ?> parameters) {
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
    public QueryStatistics query(String cypher, Map<String, ?> parameters, boolean readOnly) {

        validateQuery(cypher, parameters, readOnly);

        //If readOnly=true, just execute the query. If false, execute the query and return stats as well
        if(readOnly) {
            return new QueryStatisticsModel(executeAndMap(null, cypher, parameters, new MapRowModelMapper()),null);
        }
        else {
            RowModelStatisticsRequest parameterisedStatement = new DefaultRowModelStatisticsRequest(cypher, parameters);
            try (Response<RowStatistics> response = session.requestHandler().execute(parameterisedStatement)) {
                RowStatistics result = response.next();
                RowModelMapper rowModelMapper = new MapRowModelMapper();
                Collection rowResult = new LinkedHashSet();
                for (Iterator<Object> iterator = result.getRows().iterator(); iterator.hasNext(); ) {
                    List next =  (List) iterator.next();
                    rowModelMapper.mapIntoResult(rowResult, next.toArray(), response.columns());
                }
                return new QueryStatisticsModel(rowResult, result.getStats());

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

        if (type != null && session.metaData().classInfo(type.getSimpleName()) != null) {
            AbstractRequest qry = new DefaultGraphModelRequest(cypher, parameters);
            try (Response<Graph> response = session.requestHandler().execute((GraphModelRequest) qry)) {
                return session.responseHandler().loadAll(type, response);
            }
        } else {
            DefaultRowModelRequest qry = new DefaultRowModelRequest(cypher, parameters);
            try (Response<Row> response = session.requestHandler().execute(qry)) {

                String[] variables = response.columns();

                Collection<T> result = new ArrayList<>();
                Row rowModel;
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

        DefaultRowModelRequest countStatement = new AggregateStatements().countNodesLabelledWith(classInfo.labels());
//        session.ensureTransaction();

        try (Response<Row> response = session.requestHandler().execute(countStatement)) {
            Row queryResult = response.next();
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

    private class QueryStatisticsModel implements QueryStatistics {

        private Iterable<Map<String,Object>> result;
        private Statistics queryStatistics;

        public QueryStatisticsModel(Iterable<Map<String, Object>> result, Statistics queryStatistics) {
            this.result = result;
            this.queryStatistics = queryStatistics;
        }

        @Override
        public Iterator<Map<String,Object>> iterator() {
            return result.iterator();
        }

        @Override
        public Iterable<Map<String,Object>> model() {
            return result;
        }

        @Override
        public Statistics statistics() {
            return queryStatistics;
        }
    }
}
