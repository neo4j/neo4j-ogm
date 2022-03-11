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
package org.neo4j.ogm.session.delegates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.context.GraphRowModelMapper;
import org.neo4j.ogm.context.RestModelMapper;
import org.neo4j.ogm.context.RestStatisticsModel;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.cypher.query.DefaultGraphModelRequest;
import org.neo4j.ogm.cypher.query.DefaultRestModelRequest;
import org.neo4j.ogm.cypher.query.DefaultRowModelRequest;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DescriptorMappings;
import org.neo4j.ogm.metadata.FieldInfo;
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
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.ConvertibleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Jasper Blues
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public class ExecuteQueriesDelegate extends SessionDelegate {

    private static final String OGM_READ_ONLY_HINT = "/*+ OGM READ_ONLY */";
    private static final Pattern WRITE_CYPHER_KEYWORDS = Pattern
        .compile("\\b(CREATE|MERGE|SET|DELETE|REMOVE|DROP|CALL)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Set<Class<?>> VOID_TYPES = new HashSet<>(Arrays.asList(Void.class, void.class));
    // This is using the Neo4jSession on purpose to retrieve the logger.
    // This delegate here used Neo4jSession#warn to log warnings about possible
    // write queries.
    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jSession.class);

    public ExecuteQueriesDelegate(Neo4jSession session) {
        super(session);
    }

    public <T> T queryForObject(Class<T> type, String cypher, Map<String, ?> parameters) {
        Iterable<T> results = query(type, cypher, parameters);

        int cnt = 0;
        T returnedObject = null;
        for (T next : results) {
            if (returnedObject == null) {
                returnedObject = next;
            }
            ++cnt;
        }

        if (cnt == 0) {
            return null;
        }

        if (cnt > 1) {
            throw new RuntimeException("Result not of expected size. Expected 1 row but found " + cnt);
        }

        if (returnedObject == null || type.isAssignableFrom(returnedObject.getClass())) {
            return returnedObject;
        }

        String typeOfResult = returnedObject.getClass().getName();
        String wantedType = type.getName();
        String message = String.format(
            "Cannot map %s to %s. This can be caused by missing registration of %s.",
            typeOfResult, wantedType, wantedType
        );

        throw new MappingException(message);
    }

    public <T> Iterable<T> query(Class<T> type, String cypher, Map<String, ?> parameters) {
        validateQuery(cypher, parameters, false); //we'll allow modifying statements
        if (type == null || VOID_TYPES.contains(type)) {
            throw new RuntimeException("Supplied type must not be null or void.");
        }
        return executeAndMap(type, cypher, parameters);
    }

    public Result query(String cypher, Map<String, ?> parameters, boolean readOnly) {

        validateQuery(cypher, parameters, readOnly);

        if (mayBeReadWrite(cypher)) {
            // While an update query may not return objects, it has enough changes
            // to modify all entities in the context, so we must flush it either way.
            session.clear();
        }

        RestModelRequest request = new DefaultRestModelRequest(cypher, parameters);
        RestModelMapper mapper = new RestModelMapper(session.metaData(), session.context(),
            session.getEntityInstantiator());

        return session.doInTransaction(() -> {

            try (Response<RestModel> response = session.requestHandler().execute(request)) {
                RestStatisticsModel restStatisticsModel = mapper.map(response);

                if (readOnly) {
                    return new QueryResultModel(restStatisticsModel.getResult(), null);
                } else {
                    return new QueryResultModel(restStatisticsModel.getResult(), restStatisticsModel.getStatistics());
                }
            }
        }, readOnly ? Transaction.Type.READ_ONLY : Transaction.Type.READ_WRITE);
    }

    private <T> Iterable<T> executeAndMap(Class<T> type, String cypher, Map<String, ?> parameters) {

        return session.doInTransaction(() -> {

            // While an update query may not return objects, it has enough changes
            // to modify all entities in the context, so we must flush it either way.
            if (mayBeReadWrite(cypher)) {
                session.clear();
            }

            if (type != null && session.metaData().classInfo(type.getName()) != null) {

                // Things that can be mapped to entities
                GraphModelRequest request = new DefaultGraphModelRequest(cypher, parameters);
                try (Response<GraphModel> response = session.requestHandler().execute(request)) {
                    return new GraphRowModelMapper(session.metaData(), session.context(),
                        session.getEntityInstantiator())
                        .map(type, response);
                }
            } else {
                // Scalar mappings
                RowModelRequest request = new DefaultRowModelRequest(cypher, parameters);
                try (Response<RowModel> response = session.requestHandler().execute(request)) {
                    return mapScalarResponse(type, response);
                }
            }
        }, Transaction.Type.READ_WRITE);
    }

    private static <T> Iterable<T> mapScalarResponse(Class<T> type, Response<RowModel> response) {

        // We need to execute the request in any case, but can skip processing the result when
        // it's not assignable to the requested type.
        Collection<T> result;
        if (VOID_TYPES.contains(type)) {
            result = Collections.emptyList();
        } else {
            result = new ArrayList<>();
            RowModel model;
            while ((model = response.next()) != null) {
                result.add(extractColumnValue(type, model));
            }
        }
        return result;
    }

    private static <T> T extractColumnValue(Class<T> type, RowModel model) {

        if (model.variables().length > 1) {
            throw new RuntimeException(
                "Scalar response queries must only return one column. Make sure your cypher query only returns one item.");
        }
        final Object o = model.getValues()[0];
        return Optional.ofNullable(ConvertibleTypes.REGISTRY.get(type.getCanonicalName()))
            .map(ac -> (AttributeConverter<T, Object>) (type.isArray() ? ac.forArray : ac.forScalar))
            .map(c -> c.toEntityAttribute(o))
            .orElse((T) Utils.coerceTypes(type, o));
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

    static boolean mayBeReadWrite(String cypher) {
        if (cypher.contains(OGM_READ_ONLY_HINT)) {
            return false;
        }
        Matcher matcher = WRITE_CYPHER_KEYWORDS.matcher(cypher);
        return matcher.find();
    }

    private void validateQuery(String cypher, Map<String, ?> parameters, boolean readOnly) {

        if (LOGGER.isDebugEnabled() && readOnly && mayBeReadWrite(cypher)) {
            LOGGER.debug(
                "Thread {}: Cypher query contains keywords that indicate a writing query but OGM is going to use a read only transaction as requested, so the query might fail.",
                Thread.currentThread().getId());
        }

        if (cypher == null || cypher.isEmpty()) {
            throw new RuntimeException("Supplied cypher statement must not be null or empty.");
        }

        if (parameters == null) {
            throw new RuntimeException("Supplied Parameters cannot be null.");
        }
    }
}
