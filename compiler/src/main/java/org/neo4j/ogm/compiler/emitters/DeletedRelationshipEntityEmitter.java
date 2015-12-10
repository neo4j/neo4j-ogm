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

/**
 * @author Luanne Misquitta
 */
public class DeletedRelationshipEntityEmitter implements CypherEmitter {

	private Set<Edge> deletedEdges;

	public DeletedRelationshipEntityEmitter(Set<Edge> deletedEdges) {
		this.deletedEdges = deletedEdges;
	}

	@Override
	public void emit(StringBuilder queryBuilder, Map<String, Object> parameters) {
		if (deletedEdges != null && deletedEdges.size() > 0) {

			queryBuilder.append("START r=rel({relIds}) DELETE r");

			List<Long> relIds = new ArrayList<>(deletedEdges.size());
			List<Map> rows = new ArrayList<>();
			for (Edge edge : deletedEdges) {
				Map<String, Object> rowMap = new HashMap<>();
				rowMap.put("relId", edge.getId());
				rows.add(rowMap);
				relIds.add(edge.getId());
			}
			parameters.put("rows", rows);
			parameters.put("relIds", relIds);
		}
	}
}
