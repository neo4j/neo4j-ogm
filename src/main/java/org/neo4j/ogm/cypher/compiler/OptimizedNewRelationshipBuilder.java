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

package org.neo4j.ogm.cypher.compiler;

import java.util.Map;
import java.util.Set;

/**
 * CypherEmitter that produces an optimized Cypher query used to create relationships between already persisted nodes.
 * To be refactored in OGM 2.0
 * Does not cater to relationship entities. Also does not yet emit a separate statement for each rel type to be created.
 * @author Luanne Misquitta
 */
public class OptimizedNewRelationshipBuilder extends NewRelationshipBuilder {

	//private String type;

	public OptimizedNewRelationshipBuilder(String type) {
		super(null);  // hmmm...

        this.type = type;
	}

	@Override
	public boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack) {

		/*
            UNWIND {rows} as row
            MATCH (startNode) WHERE ID(startNode)={row.startNodeId}
            MATCH (endNode) WHERE ID(endNode)={row.endNodeId}
            MERGE (startNode)-[rel:`TYPE`]->endNode)
            RETURN row.startNodeRef, row.endNodeRef, row.relRef, row.startNodeId, row.endNodeId, ID(rel)
         */
		String rowsParameter = "rows" + type;
		queryBuilder.append(" UNWIND {").append(rowsParameter).append("} as row ")
				.append("MATCH (startNode) WHERE ID(startNode)=row.startNodeId ")
				.append("MATCH (endNode) WHERE ID(endNode)=row.endNodeId ")
				.append("MERGE (startNode)-[rel:`").append(type).append("`]->(endNode) ")
				.append("RETURN row.relRef as relRef, ID(rel) as relId ");

		return true;
	}
}
