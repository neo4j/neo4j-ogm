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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.neo4j.ogm.compiler.CompileContext;
import org.neo4j.ogm.context.EntityGraphMapper;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.event.SaveEventDelegate;
import org.neo4j.ogm.session.request.RequestExecutor;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class SaveDelegate {

	private final Neo4jSession session;
	private final RequestExecutor requestExecutor;

	public SaveDelegate(Neo4jSession neo4jSession) {
		this.session = neo4jSession;
		requestExecutor = new RequestExecutor(neo4jSession);
	}

	public <T> void save(T object) {
		save(object, -1); // default : full tree of changed objects
	}

	public <T> void save(T object, int depth) {

		SaveEventDelegate eventsDelegate = new SaveEventDelegate(session);

		if (object.getClass().isArray() || Iterable.class.isAssignableFrom(object.getClass())) {
			Collection<T> objects;
			if (object.getClass().isArray()) {
				int length = Array.getLength(object);
				objects = new ArrayList<>(length);
				for (int i = 0; i < length; i++) {
					T arrayElement = (T) Array.get(object, i);
					objects.add(arrayElement);
				}
			} else {
				objects = (Collection<T>) object;
			}
			List<CompileContext> contexts = new ArrayList<>();
			for (Object element : objects) {
				if (session.eventsEnabled()) {
					eventsDelegate.preSave(object);
				}
				contexts.add(new EntityGraphMapper(session.metaData(), session.context()).map(element, depth));
			}
			requestExecutor.executeSave(contexts);
			if (session.eventsEnabled()) {
				eventsDelegate.postSave();
			}
		} else {
			ClassInfo classInfo = session.metaData().classInfo(object);
			if (classInfo != null) {

				if (session.eventsEnabled()) {
					eventsDelegate.preSave(object);
				}

				CompileContext context = new EntityGraphMapper(session.metaData(), session.context()).map(object, depth);

				requestExecutor.executeSave(context);

				if (session.eventsEnabled()) {
					eventsDelegate.postSave();
				}
			} else {
				session.warn(object.getClass().getName() + " is not an instance of a persistable class");
			}
		}
	}
}
