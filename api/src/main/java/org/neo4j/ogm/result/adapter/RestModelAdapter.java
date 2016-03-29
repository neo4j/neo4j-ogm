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

package org.neo4j.ogm.result.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.RelationshipModel;

/**
 * Adapt embedded response to a NodeModels, RelationshipModels, and objects
 * @author Luanne Misquitta
 */
public abstract class RestModelAdapter implements ResultAdapter<Map<String,Object>, Map<String,Object>> {

	@Override
	public Map<String,Object> adapt(Map<String, Object> result) {
		Map<String,Object> adaptedResults = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : result.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Collection) {
				List<Object> adaptedValues = new ArrayList<>();
				List<Object> values = (List) value;
				for (Object element : values) {
					adaptedValues.add(processData(element));
				}
				adaptedResults.put(entry.getKey(), adaptedValues);
			}
			else {
				adaptedResults.put(entry.getKey(), processData(value));
			}
		}

		return adaptedResults;
	}

	private Object processData(Object element) {
		if (isNode(element)) {
			return buildNode(element);
		}
		if (isRelationship(element)) {
			return buildRelationship(element);
		}
		return element;
	}

	private NodeModel buildNode(Object node) {
		NodeModel nodeModel = new NodeModel();
		nodeModel.setId(nodeId(node));
		List<String> labels = labels(node);
		nodeModel.setLabels(labels.toArray(new String[labels.size()]));
		nodeModel.setProperties(properties(node));
		return nodeModel;
	}

	private RelationshipModel buildRelationship(Object relationship) {
		RelationshipModel relationshipModel = new RelationshipModel();
		relationshipModel.setId(relationshipId(relationship));
		relationshipModel.setStartNode(startNodeId(relationship));
		relationshipModel.setEndNode(endNodeId(relationship));
		relationshipModel.setType(relationshipType(relationship));
		relationshipModel.setProperties(properties(relationship));
		return relationshipModel;
	}

	public abstract boolean isNode(Object value);

	public abstract boolean isRelationship(Object value);

	public abstract long nodeId(Object node);

	public abstract List<String> labels(Object node);

	public abstract long relationshipId(Object relationship);

	public abstract String relationshipType(Object relationship);

	public abstract Long startNodeId(Object relationship);

	public abstract Long endNodeId(Object relationship);

	public abstract Map<String,Object> properties(Object container);

}
