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

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.session.Neo4jSession;

/**
 * @author Vince Bickers
 */
public class LoadByInstancesDelegate extends SessionDelegate {

    public LoadByInstancesDelegate(Neo4jSession session) {
        super(session);
    }

    public <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, Pagination pagination, int depth) {

        if (objects == null || objects.isEmpty()) {
            return objects;
        }

        Set<Serializable> ids = new LinkedHashSet<>();
        Class type = objects.iterator().next().getClass();
        ClassInfo classInfo = session.metaData().classInfo(type.getName());
        for (Object o : objects) {
            FieldInfo idField;
            if (classInfo.hasPrimaryIndexField()) {
                idField = classInfo.primaryIndexField();
            } else {
                idField = classInfo.identityField();
            }
            ids.add((Serializable) idField.readProperty(o));
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
