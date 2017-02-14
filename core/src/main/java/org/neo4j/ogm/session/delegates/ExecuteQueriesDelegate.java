/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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
package org.neo4j.ogm.session.delegates;


import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.context.*;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.cypher.query.DefaultGraphModelRequest;
import org.neo4j.ogm.cypher.query.DefaultRestModelRequest;
import org.neo4j.ogm.cypher.query.DefaultRowModelRequest;
import org.neo4j.ogm.metadata.ClassInfo;
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
import org.neo4j.ogm.utils.ClassUtils;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Jasper Blues
 */
public class ExecuteQueriesDelegate {

	private static final Pattern WRITE_CYPHER_KEYWORDS = Pattern.compile("\\b(CREATE|MERGE|SET|DELETE|REMOVE|DROP)\\b");

	private final Neo4jSession session;

	public ExecuteQueriesDelegate(Neo4jSession neo4jSession) {
		this.session = neo4jSession;
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

		return results.iterator().next();
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
		ResponseMapper mapper = new RestModelMapper(new GraphEntityMapper(session.metaData(), session.context()), session.metaData());

		try (Response<RestModel> response = session.requestHandler().execute(request)) {
			Iterable<RestStatisticsModel> mappedModel = mapper.map(null, response);
			RestStatisticsModel restStatisticsModel = mappedModel.iterator().next();

			if (readOnly) {
				return new QueryResultModel(restStatisticsModel.getResult(), null);
			} else {
				return new QueryResultModel(restStatisticsModel.getResult(), restStatisticsModel.getStatistics());
			}
		}
	}

	private <T> Iterable<T> executeAndMap(Class<T> type, String cypher, Map<String, ?> parameters, ResponseMapper mapper) {

		if (type != null && session.metaData().classInfo(type.getSimpleName()) != null) {
			GraphModelRequest request = new DefaultGraphModelRequest(cypher, parameters);
			try (Response<GraphModel> response = session.requestHandler().execute(request)) {
				return new GraphEntityMapper(session.metaData(), session.context()).map(type, response);
			}
		} else {
			RowModelRequest request = new DefaultRowModelRequest(cypher, parameters);
			try (Response<RowModel> response = session.requestHandler().execute(request)) {
				return mapper.map(type, response);
			}
		}
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
				if (fieldInfo.hasAnnotation(StartNode.CLASS)) {
					startNodeInfo = session.metaData().classInfo(ClassUtils.getType(fieldInfo.getTypeDescriptor()).getName());
				} else if (fieldInfo.hasAnnotation(EndNode.CLASS)) {
					endNodeInfo = session.metaData().classInfo(ClassUtils.getType(fieldInfo.getTypeDescriptor()).getName());
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
			countStatement = new CountStatements().countNodes(labels);
		}
		try (Response<RowModel> response = session.requestHandler().execute((RowModelRequest) countStatement)) {
			RowModel queryResult = response.next();
			return queryResult == null ? 0 : ((Number) queryResult.getValues()[0]).longValue();
		}
	}

	public long count(Class<?> clazz, Iterable<Filter> filters) {

		ClassInfo classInfo = session.metaData().classInfo(clazz.getSimpleName());

		if (classInfo != null) {

			session.resolvePropertyAnnotations(clazz, filters);

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
	 * @param query the CypherQuery that will count objects according to some filter criteria
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
