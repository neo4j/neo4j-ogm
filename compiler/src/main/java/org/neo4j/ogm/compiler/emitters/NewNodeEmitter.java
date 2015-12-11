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
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.model.Property;

/**
 * @author Luanne Misquitta
 */
public class NewNodeEmitter implements CypherEmitter {

	Set<Node> newNodes;

	public NewNodeEmitter(Set<Node> newNodes) {
		this.newNodes = newNodes;
	}

	@Override
	public void emit(StringBuilder queryBuilder, Map<String, Object> parameters) {
		if (newNodes != null && newNodes.size() > 0) {
			Node firstNode = newNodes.iterator().next();

			queryBuilder.append("UNWIND {rows} as row ")
					.append("CREATE (n");
			for (String label : firstNode.getLabels()) {
				queryBuilder.append(":`").append(label).append("`");
			}
			queryBuilder.append(") SET n=row.props RETURN row.nodeRef as nodeRef, ID(n) as nodeId");
			List<Map> rows = new ArrayList<>();
			for (Node node : newNodes) {
				Map<String, Object> rowMap = new HashMap<>();
				rowMap.put("nodeRef", node.getId());
				Map<String, Object> props = new HashMap<>();
				for (Property property : node.getPropertyList()) {
					if (property.getValue() != null) {
						props.put((String) property.getKey(), property.getValue());
					}
				}
				rowMap.put("props", props);
				rows.add(rowMap);
			}
			parameters.put("rows", rows);
		}
	}
}
