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

import java.io.Serializable;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.context.GraphEntityMapper;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.request.GraphModelRequest;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jException;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.request.strategy.QueryStatements;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
public class LoadOneDelegate {

	private Neo4jSession session;

	public LoadOneDelegate(Neo4jSession session) {
		this.session = session;
	}

	public <T, ID extends Serializable> T load(Class<T> type, ID id) {
		return load(type, id, 1);
	}

	public <T, ID extends Serializable> T load(Class<T> type, ID id, int depth) {

		final FieldInfo primaryIndexField = session.metaData().classInfo(type.getName()).primaryIndexField();
		if (primaryIndexField != null && !primaryIndexField.isTypeOf(id.getClass())) {
			throw new Neo4jException("Supplied id does not match primary index type on supplied class.");
		}

		QueryStatements queryStatements = session.queryStatementsFor(type);
		PagingAndSortingQuery qry = queryStatements.findOne(id, depth);

		try (Response<GraphModel> response = session.requestHandler().execute((GraphModelRequest) qry)) {
			new GraphEntityMapper(session.metaData(), session.context()).map(type, response);
			return lookup(type, id);
		}
	}

	private <T, U> T lookup(Class<T> type, U id) {
		Object ref;
		ClassInfo typeInfo = session.metaData().classInfo(type.getName());

		if (typeInfo.annotationsInfo().get(RelationshipEntity.CLASS) == null) {
			ref = session.context().getNodeEntity(id);
		} else {
			// Coercing to Long. identityField.convertedType() yields no parametrised type to call cast() with.
			// But we know this will always be Long.
			ref = session.context().getRelationshipEntity((Long) id);
		}
		try {
			return type.cast(ref);
		} catch (ClassCastException cce) {
			return null;
		}
	}
}
