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

package org.neo4j.ogm.drivers.embedded.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.ogm.result.ResultAdapter;

/**
 * @author Luanne Misquitta
 */
public class RestModelAdapter implements ResultAdapter<Map<String,Object>, List<Object>> {

	String[] columns;

	@Override
	public List<Object> adapt(Map<String, Object> result) {
		List<Object> queryResult = new ArrayList<>(result.size());
		for (String column : columns) {
			Object value = result.get(column);

			if (value instanceof Collection) {
				List<Object> queryResultList  = new ArrayList<>();
				for (Object data : ((List)value)) {
					processData(data, queryResultList);
				}
				queryResult.add(queryResultList);
			}
			else {
				Map<String,Object> queryResultRow = new HashMap<>();
				processData(value, queryResult , queryResultRow);
			}
		}
		return queryResult;
	}

	private void processData(Object value, List<Object> queryResultList) {
		Map<String,Object> queryResultRow = new HashMap<>();
		if (value instanceof Node) {
			buildNode((Node)value, queryResultRow);
			queryResultList.add(queryResultRow);
			return;
		}
		if (value instanceof Relationship) {
			buildRelationship((Relationship)value, queryResultRow);
			queryResultList.add(queryResultRow);
			return;
		}
		queryResultList.add(value);
	}

	private void processData(Object value, List<Object> queryResult, Map<String, Object> queryResultRow) {
		if (value instanceof Node) {
			buildNode((Node)value, queryResultRow);
			queryResult.add(queryResultRow);
			return;
		}
		if (value instanceof Relationship) {
			buildRelationship((Relationship)value, queryResultRow);
			queryResult.add(queryResultRow);
			return;
		}
		queryResult.add(value);
	}

	public void setColumns(String[] columns) {
		this.columns = columns;
	}

	private void buildNode(Node node, Map<String,Object> result) {
		Map<String,Object> metadata = new HashMap<>();
		metadata.put("id", node.getId());
		List<String> labels = new ArrayList<>();
		for (Label label : node.getLabels()) {
			labels.add(label.name());
		}
		metadata.put("labels", labels);
		result.put("metadata", metadata);
		result.put("data", node.getAllProperties());
	}

	private void buildRelationship(Relationship relationship, Map<String,Object> result) {
		Map<String,Object> metadata = new HashMap<>();
		metadata.put("id", relationship.getId());
		metadata.put("type", relationship.getType().name());
		result.put("metadata", metadata);
		result.put("data", relationship.getAllProperties());
		result.put("start","/" + relationship.getStartNode().getId());
		result.put("end", "/" + relationship.getEndNode().getId());
	}
}
