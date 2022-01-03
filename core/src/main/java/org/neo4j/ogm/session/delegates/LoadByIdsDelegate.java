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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.neo4j.ogm.context.GraphRowModelMapper;
import org.neo4j.ogm.cypher.query.DefaultGraphModelRequest;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.request.GraphModelRequest;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.request.strategy.QueryStatements;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.utils.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class LoadByIdsDelegate extends SessionDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(LoadByIdsDelegate.class);

    public LoadByIdsDelegate(Neo4jSession session) {
        super(session);
    }

    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder,
        Pagination pagination, int depth) {

        Optional<String> labelsOrType = session.determineLabelsOrTypeForLoading(type);
        if (!labelsOrType.isPresent()) {
            LOG.warn("Unable to find database label for entity " + type.getName()
                + " : no results will be returned. Make sure the class is registered, "
                + "and not abstract without @NodeEntity annotation");
            return Collections.emptyList();
        }

        QueryStatements<ID> queryStatements = session.queryStatementsFor(type, depth);

        ClassInfo classInfo = session.metaData().classInfo(type.getName());
        PagingAndSortingQuery qry = queryStatements.findAllByType(labelsOrType.get(), convertIfNeeded(classInfo, ids), depth)
            .setSortOrder(sortOrder)
            .setPagination(pagination);

        GraphModelRequest request = new DefaultGraphModelRequest(qry.getStatement(), qry.getParameters());
        return session.doInTransaction(() -> {
            try (Response<GraphModel> response = session.requestHandler().execute(request)) {
                Iterable<T> mapped = new GraphRowModelMapper(session.metaData(), session.context(),
                    session.getEntityInstantiator()).map(type, response);

                if (sortOrder.sortClauses().isEmpty()) {
                    return sortResultsByIds(type, ids, mapped);
                }
                Set<T> results = new LinkedHashSet<>();
                for (T entity : mapped) {
                    if (includeMappedEntity(ids, entity)) {
                        results.add(entity);
                    }
                }
                return results;
            }
        }, Transaction.Type.READ_ONLY);
    }

    private <T, ID extends Serializable> Set<T> sortResultsByIds(Class<T> type, Collection<ID> ids,
        Iterable<T> mapped) {
        Map<ID, T> items = new HashMap<>();
        ClassInfo classInfo = session.metaData().classInfo(type.getName());

        Function<Object, Optional<Object>> primaryIndexOrIdReader
            = classInfo.getPrimaryIndexOrIdReader();

        for (T t : mapped) {
            primaryIndexOrIdReader.apply(t)
                .ifPresent(id -> items.put((ID) id, t));
        }

        Set<T> results = new LinkedHashSet<>();
        for (ID id : ids) {
            T item = items.get(id);
            if (item != null) {
                results.add(item);
            }
        }
        return results;
    }

    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids) {
        return loadAll(type, ids, new SortOrder(), null, 1);
    }

    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, int depth) {
        return loadAll(type, ids, new SortOrder(), null, depth);
    }

    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder) {
        return loadAll(type, ids, sortOrder, null, 1);
    }

    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder,
        int depth) {
        return loadAll(type, ids, sortOrder, null, depth);
    }

    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, Pagination paging) {
        return loadAll(type, ids, new SortOrder(), paging, 1);
    }

    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, Pagination paging,
        int depth) {
        return loadAll(type, ids, new SortOrder(), paging, depth);
    }

    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder,
        Pagination pagination) {
        return loadAll(type, ids, sortOrder, pagination, 1);
    }

    private <T, ID extends Serializable> boolean includeMappedEntity(Collection<ID> ids, T mapped) {

        final ClassInfo classInfo = session.metaData().classInfo(mapped);

        if (classInfo.hasPrimaryIndexField()) {
            final Object primaryIndexValue = classInfo.readPrimaryIndexValueOf(mapped);
            if (ids.contains(primaryIndexValue)) {
                return true;
            }
        }
        Object id = EntityUtils.identity(mapped, session.metaData());
        return ids.contains(id);
    }
}
