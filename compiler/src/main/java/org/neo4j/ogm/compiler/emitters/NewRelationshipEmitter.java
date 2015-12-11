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

package org.neo4j.ogm.compiler.emitters;

import java.util.*;

import org.neo4j.ogm.compiler.CypherEmitter;
import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.model.Property;

/**
 * @author Luanne Misquitta
 */
public class NewRelationshipEmitter implements CypherEmitter {

	private Set<Edge> edges;

	public NewRelationshipEmitter(Set<Edge> edges) {
		this.edges = edges;
	}

	@Override
	public void emit(StringBuilder queryBuilder, Map<String, Object> parameters) {
		boolean hasProperties = false;
		if (edges != null && edges.size() > 0) {
			Edge firstEdge = edges.iterator().next();
			String relType = firstEdge.getType();
			if (firstEdge.getPropertyList().size() > 0) {
				hasProperties = true;
			}

			queryBuilder.append("UNWIND {rows} as row ")
					.append("MATCH (startNode) WHERE ID(startNode) = row.startNodeId ")
					.append("MATCH (endNode) WHERE ID(endNode) = row.endNodeId ")
					.append("MERGE (startNode)-[rel:`").append(relType).append("`");

			if (hasProperties) {
				boolean firstProperty = true;
				queryBuilder.append("{ ");
				Set<String> sortedProperties = new TreeSet<>();
				for (Property property : firstEdge.getPropertyList()) {
					sortedProperties.add("`" + property.getKey() + "`: row.props." + property.getKey());
				}

				for (String propertyString : sortedProperties) {
					if (!firstProperty) {
						queryBuilder.append(", ");
					}
					queryBuilder.append(propertyString);
					firstProperty = false;
				}
				queryBuilder.append("}");
			}

			queryBuilder.append("]->(endNode) ")
					.append("RETURN row.relRef as relRefId, ID(rel) as relId");

			List<Map> rows = new ArrayList<>();
			for (Edge edge : edges) {
				Map<String, Object> rowMap = new HashMap<>();
				rowMap.put("startNodeId", edge.getStartNode());
				rowMap.put("endNodeId", edge.getEndNode());
				rowMap.put("relRef", edge.getId());
				if (hasProperties) {
					Map<String, Object> props = new HashMap<>();
					for (Property property : edge.getPropertyList()) {
						props.put((String) property.getKey(), property.getValue());
					}
					rowMap.put("props", props);
				}
				rows.add(rowMap);
			}
			parameters.put("rows", rows);
		}
	}
}
