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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.entity.io.FieldWriter;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.session.Neo4jSession;

/**
 * @author Vince Bickers
 */
public class LoadByInstancesDelegate {

	private final Neo4jSession session;

	public LoadByInstancesDelegate(Neo4jSession session) {
		this.session = session;
	}

	public <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, Pagination pagination, int depth) {

		if (objects == null || objects.isEmpty()) {
			return objects;
		}

		Set<Long> ids = new HashSet<>();
		Class type = objects.iterator().next().getClass();
		ClassInfo classInfo = session.metaData().classInfo(type.getName());
		Field identityField = classInfo.getField(classInfo.identityField());

		for (Object o : objects) {
			ids.add((Long) FieldWriter.read(identityField, o));
		}
		return session.loadAll(type, ids, sortOrder, pagination, depth);
	}

	public <T> Collection<T> loadAll(Collection<T> objects) {
		return loadAll(objects, new SortOrder(), null, 1);
	}

	public <T> Collection<T> loadAll(Collection<T> objects, int depth) {
		return loadAll(objects, new SortOrder(), null, depth);
	}

	public <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder) {
		return loadAll(objects, sortOrder, null, 1);
	}

	public <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, int depth) {
		return loadAll(objects, sortOrder, null, depth);
	}

	public <T> Collection<T> loadAll(Collection<T> objects, Pagination pagination) {
		return loadAll(objects, new SortOrder(), pagination, 1);
	}

	public <T> Collection<T> loadAll(Collection<T> objects, Pagination pagination, int depth) {
		return loadAll(objects, new SortOrder(), pagination, depth);
	}

	public <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, Pagination pagination) {
		return loadAll(objects, sortOrder, pagination, 1);
	}
}
