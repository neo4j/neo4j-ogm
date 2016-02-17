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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.RelationshipModel;
import org.neo4j.ogm.result.ResultAdapter;

/**
 * Adapt embedded response to a NodeModels, RelaitonshipModels, and objects
 * @author Luanne Misquitta
 */
public class RestModelAdapter implements ResultAdapter<Map<String,Object>, Map<String,Object>> {

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
		if (element instanceof Node) {
			return buildNode((Node)element);
		}
		if (element instanceof Relationship) {
			return buildRelationship((Relationship)element);
		}
		return element;
	}

	private NodeModel buildNode(Node node) {
		NodeModel nodeModel = new NodeModel();
		nodeModel.setId(node.getId());
		List<String> labels = new ArrayList<>();
		for (Label label : node.getLabels()) {
			labels.add(label.name());
		}
		nodeModel.setLabels(labels.toArray(new String[labels.size()]));
		nodeModel.setProperties(node.getAllProperties());
		return nodeModel;
	}

	private RelationshipModel buildRelationship(Relationship relationship) {
		RelationshipModel relationshipModel = new RelationshipModel();
		relationshipModel.setId(relationship.getId());
		relationshipModel.setStartNode(relationship.getStartNode().getId());
		relationshipModel.setEndNode(relationship.getEndNode().getId());
		relationshipModel.setType(relationship.getType().name());
		relationshipModel.setProperties(relationship.getAllProperties());
		return relationshipModel;
	}
}
