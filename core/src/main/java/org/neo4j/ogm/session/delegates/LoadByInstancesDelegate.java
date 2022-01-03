/*
 * Copyright (c) 2002-2022 "Neo4j,"
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

import static java.util.stream.Collectors.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MetaData;
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

        ClassInfo commonClassInfo = findCommonClassInfo(objects);
        Function<Object, Optional<Object>> primaryIndexOrIdReader
            = commonClassInfo.getPrimaryIndexOrIdReader();

        Set<Serializable> ids = objects.stream()
            .map(primaryIndexOrIdReader::apply)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Serializable.class::cast)
            .collect(toCollection(LinkedHashSet::new));

        return session.loadAll((Class<T>) commonClassInfo.getUnderlyingClass(), ids, sortOrder, pagination, depth);
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

    /**
     * @param objects
     * @param <T>
     * @return A class info that is part of the hierarchy of the distinct object types contained in {@code objects}.
     */
    private <T> ClassInfo findCommonClassInfo(Collection<T> objects) {
        MetaData metaData = session.metaData();
        Set<ClassInfo> infos = objects.stream()
            .map(Object::getClass) //
            .distinct() //
            .map(metaData::classInfo) //
            .map(LoadByInstancesDelegate::getRootClassInfo) //
            .collect(toSet());

        if (infos.size() != 1) {
            throw new MappingException("Can't find single supertype for " + infos);
        }

        return infos.iterator().next();
    }

    /**
     * @param classInfo
     * @return The topmost element of a mapped class hierarchy.
     */
    private static ClassInfo getRootClassInfo(ClassInfo classInfo) {

        ClassInfo current = classInfo;
        while (current.directSuperclass() != null) {
            current = current.directSuperclass();
        }
        return current;
    }
}
