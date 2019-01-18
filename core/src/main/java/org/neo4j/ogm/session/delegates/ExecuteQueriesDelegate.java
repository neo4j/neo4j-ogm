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
package org.neo4j.ogm.session.delegates;

import static org.neo4j.ogm.metadata.ClassInfo.*;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.context.EntityRowModelMapper;
import org.neo4j.ogm.context.GraphEntityMapper;
import org.neo4j.ogm.context.GraphRowModelMapper;
import org.neo4j.ogm.context.ResponseMapper;
import org.neo4j.ogm.context.RestModelMapper;
import org.neo4j.ogm.context.RestStatisticsModel;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.cypher.query.DefaultGraphModelRequest;
import org.neo4j.ogm.cypher.query.DefaultRestModelRequest;
import org.neo4j.ogm.cypher.query.DefaultRowModelRequest;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.DescriptorMappings;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.RestModel;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.GraphModelRequest;
import org.neo4j.ogm.request.RestModelRequest;
import org.neo4j.ogm.request.RowModelRequest;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.QueryResultModel;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.session.request.strategy.impl.CountStatements;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Jasper Blues
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public class ExecuteQueriesDelegate extends SessionDelegate {

    private static final Pattern WRITE_CYPHER_KEYWORDS = Pattern.compile("\\b(CREATE|MERGE|SET|DELETE|REMOVE|DROP)\\b");

    public ExecuteQueriesDelegate(Neo4jSession session) {
        super(session);
    }

    public <T> T queryForObject(Class<T> type, String cypher, Map<String, ?> parameters) {
        Iterable<T> results = query(type, cypher, parameters);

        int resultSize = Utils.size(results);

        if (resultSize < 1) {
            return null;
        }

        if (resultSize > 1) {
            throw new RuntimeException("Result not of expected size. Expected 1 row but found " + resultSize);
        }

        T next = results.iterator().next();

        if (!next.getClass().isAssignableFrom(type)) {

            String typeOfResult = next.getClass().getName();
            String wantedType = type.getName();
            String message = String.format(
                "Cannot map %s to %s. This can be caused by missing registration of %s.",
                typeOfResult, wantedType, wantedType
            );

            throw new MappingException(message);
        }

        return next;
    }

    public Result query(String cypher, Map<String, ?> parameters) {
        return query(cypher, parameters, isReadOnly(cypher));
    }

    public <T> Iterable<T> query(Class<T> type, String cypher, Map<String, ?> parameters) {
        validateQuery(cypher, parameters, false); //we'll allow modifying statements
        if (type == null || type.equals(Void.class)) {
            throw new RuntimeException("Supplied type must not be null or void.");
        }
        return executeAndMap(type, cypher, parameters, new EntityRowModelMapper());
    }

    public Result query(String cypher, Map<String, ?> parameters, boolean readOnly) {

        validateQuery(cypher, parameters, readOnly);

        RestModelRequest request = new DefaultRestModelRequest(cypher, parameters);
        RestModelMapper mapper = new RestModelMapper(new GraphEntityMapper(session.metaData(), session.context(),
            session.getEntityInstantiator()),
            session.metaData());

        return session.doInTransaction(() -> {

            try (Response<RestModel> response = session.requestHandler().execute(request)) {
                RestStatisticsModel restStatisticsModel = mapper.map(response);

                if (readOnly) {
                    return new QueryResultModel(restStatisticsModel.getResult(), null);
                } else {
                    return new QueryResultModel(restStatisticsModel.getResult(), restStatisticsModel.getStatistics());
                }
            }
        }, Transaction.Type.READ_WRITE);
    }

    private <T> Iterable<T> executeAndMap(Class<T> type, String cypher, Map<String, ?> parameters,
        ResponseMapper mapper) {

        return session.<Iterable<T>>doInTransaction(() -> {
            if (type != null && session.metaData().classInfo(deriveSimpleName(type)) != null) {
                GraphModelRequest request = new DefaultGraphModelRequest(cypher, parameters);
                try (Response<GraphModel> response = session.requestHandler().execute(request)) {
                    return new GraphRowModelMapper(session.metaData(), session.context(), session.getEntityInstantiator())
                        .map(type, response);
                }
            } else {
                RowModelRequest request = new DefaultRowModelRequest(cypher, parameters);
                try (Response<RowModel> response = session.requestHandler().execute(request)) {
                    return mapper.map(type, response);
                }
            }
        }, Transaction.Type.READ_WRITE);
    }

    public long countEntitiesOfType(Class<?> entity) {

        ClassInfo classInfo = session.metaData().classInfo(entity.getName());
        if (classInfo == null) {
            return 0;
        }

        CypherQuery countStatement;
        if (classInfo.isRelationshipEntity()) {

            ClassInfo startNodeInfo = null;
            ClassInfo endNodeInfo = null;

            for (FieldInfo fieldInfo : classInfo.fieldsInfo().fields()) {
                if (fieldInfo.hasAnnotation(StartNode.class)) {
                    startNodeInfo = session.metaData()
                        .classInfo(DescriptorMappings.getType(fieldInfo.getTypeDescriptor()).getName());
                } else if (fieldInfo.hasAnnotation(EndNode.class)) {
                    endNodeInfo = session.metaData()
                        .classInfo(DescriptorMappings.getType(fieldInfo.getTypeDescriptor()).getName());
                }
                if (endNodeInfo != null && startNodeInfo != null) {
                    break;
                }
            }

            String start = startNodeInfo.neo4jName();
            String end = endNodeInfo.neo4jName();
            String type = classInfo.neo4jName();
            countStatement = new CountStatements().countEdges(start, type, end);
        } else {
            Collection<String> labels = classInfo.staticLabels();
            if (labels.isEmpty()) {
                return 0;
            }
            countStatement = new CountStatements().countNodes(labels);
        }
        return session.doInTransaction(() -> {
            try (Response<RowModel> response = session.requestHandler().execute((RowModelRequest) countStatement)) {
                RowModel queryResult = response.next();
                return queryResult == null ? 0 : ((Number) queryResult.getValues()[0]).longValue();
            }
        }, Transaction.Type.READ_ONLY);
    }

    public long count(Class<?> clazz, Iterable<Filter> filters) {

        ClassInfo classInfo = session.metaData().classInfo(clazz.getSimpleName());

        if (classInfo != null) {

            resolvePropertyAnnotations(clazz, filters);

            CypherQuery query;

            if (classInfo.isRelationshipEntity()) {
                query = new CountStatements().countEdges(classInfo.neo4jName(), filters);
            } else {
                query = new CountStatements().countNodes(classInfo.neo4jName(), filters);
            }
            return count(query, classInfo.isRelationshipEntity());
        }

        throw new RuntimeException(clazz.getName() + " is not a persistable class");
    }

    /**
     * Executes a count query in which objects of a specific type will be counted according to some filter criteria,
     * and returns a count of matched objects to the caller.
     *
     * @param query                the CypherQuery that will count objects according to some filter criteria
     * @param isRelationshipEntity whether the objects being counted are relationship entities
     * @return a count of objects that matched the query
     */
    private Long count(CypherQuery query, boolean isRelationshipEntity) {
        String resultKey = isRelationshipEntity ? "COUNT(r0)" : "COUNT(n)";
        Result result = session.query(query.getStatement(), query.getParameters(), true); // count queries are read only
        Map<String, Object> resultMap = result.iterator().next();
        return Long.parseLong(resultMap.get(resultKey).toString());
    }

    private boolean isReadOnly(String cypher) {
        Matcher matcher = WRITE_CYPHER_KEYWORDS.matcher(cypher.toUpperCase());
        return !matcher.find();
    }

    private void validateQuery(String cypher, Map<String, ?> parameters, boolean readOnly) {

        if (readOnly && !isReadOnly(cypher)) {
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
