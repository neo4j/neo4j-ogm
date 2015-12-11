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
public class ExistingRelationshipEmitter implements CypherEmitter {

	Set<Edge> edges;

	public ExistingRelationshipEmitter(Set<Edge> edges) {
		this.edges = edges;
	}

	@Override
	public void emit(StringBuilder queryBuilder, Map<String, Object> parameters) {
		if (edges.size() > 0) {
			//queryBuilder.append("START r=rels({relIds}) SET r += ({rows}[toString(id(r))]).props"); //TODO 2.3+
			queryBuilder.append("START r=rel({relIds}) FOREACH (row in filter(row in {rows} where row.relId = id(r)) | SET r += row.props)");
			List<Long> relIds = new ArrayList<>(edges.size());
			List<Map> rows = new ArrayList<>();
			for (Edge edge : edges) {
				Map<String, Object> rowMap = new HashMap<>();
				rowMap.put("relId", edge.getId());
				Map<String, Object> props = new HashMap<>();
				for (Property property : edge.getPropertyList()) {
					if (property.getValue() != null) {
						props.put((String) property.getKey(), property.getValue());
					}
				}
				rowMap.put("props", props);
				rows.add(rowMap);
				relIds.add(edge.getId());
			}
			parameters.put("rows", rows);
			parameters.put("relIds", relIds);
		}
	}
}
