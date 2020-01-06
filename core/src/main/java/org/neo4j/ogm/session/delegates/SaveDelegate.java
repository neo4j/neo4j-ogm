/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.session.delegates;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.context.EntityGraphMapper;
import org.neo4j.ogm.cypher.compiler.CompileContext;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.request.RequestExecutor;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class SaveDelegate extends SessionDelegate {

    private final RequestExecutor requestExecutor;

    public SaveDelegate(Neo4jSession session) {
        super(session);
        requestExecutor = new RequestExecutor(session);
    }

    public <T> void save(T object) {
        save(object, -1); // default : full tree of changed objects
    }

    public <T> void save(T object, int depth) {

        SaveEventDelegate eventsDelegate = new SaveEventDelegate(session);

        if (object.getClass().isArray() || Iterable.class.isAssignableFrom(object.getClass())) {
            Iterable<T> objects;
            if (object.getClass().isArray()) {
                int length = Array.getLength(object);
                List<T> copy = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    T arrayElement = (T) Array.get(object, i);
                    copy.add(arrayElement);
                }
                objects = copy;
            } else {
                objects = (Iterable<T>) object;
            }
            EntityGraphMapper mapper = new EntityGraphMapper(session.metaData(), session.context());
            for (Object element : objects) {
                if (session.eventsEnabled()) {
                    eventsDelegate.preSave(object);
                }
                mapper.map(element, depth);
            }
            requestExecutor.executeSave(mapper.compileContext());
            if (session.eventsEnabled()) {
                eventsDelegate.postSave();
            }
        } else {
            ClassInfo classInfo = session.metaData().classInfo(object);
            if (classInfo != null) {

                if (session.eventsEnabled()) {
                    eventsDelegate.preSave(object);
                }

                CompileContext context = new EntityGraphMapper(session.metaData(), session.context())
                    .map(object, depth);

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
