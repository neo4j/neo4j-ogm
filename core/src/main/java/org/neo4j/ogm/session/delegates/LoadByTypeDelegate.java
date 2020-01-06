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

import java.util.Collection;

import org.neo4j.ogm.context.GraphEntityMapper;
import org.neo4j.ogm.context.GraphRowListModelMapper;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.DefaultGraphModelRequest;
import org.neo4j.ogm.cypher.query.DefaultGraphRowListModelRequest;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.request.GraphModelRequest;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.request.strategy.QueryStatements;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class LoadByTypeDelegate extends SessionDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(LoadByTypeDelegate.class);

    public LoadByTypeDelegate(Neo4jSession session) {
        super(session);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, Pagination pagination,
        int depth) {

        //session.ensureTransaction();
        String entityLabel = session.entityType(type.getName());
        if (entityLabel == null) {
            LOG.warn("Unable to find database label for entity " + type.getName()
                + " : no results will be returned. Make sure the class is registered, "
                + "and not abstract without @NodeEntity annotation");
        }
        QueryStatements queryStatements = session.queryStatementsFor(type, depth);

        SortOrder sortOrderWithResolvedProperties = sortOrderWithResolvedProperties(type, sortOrder);

        PagingAndSortingQuery query;
        if (filters.isEmpty()) {
            query = queryStatements.findByType(entityLabel, depth);
        } else {
            resolvePropertyAnnotations(type, filters);
            query = queryStatements.findByType(entityLabel, filters, depth);
        }

        query.setSortOrder(sortOrderWithResolvedProperties)
            .setPagination(pagination);

        return session.doInTransaction( () -> {
            if (query.needsRowResult()) {
                DefaultGraphRowListModelRequest graphRowListModelRequest = new DefaultGraphRowListModelRequest(
                query.getStatement(), query.getParameters());
                try (Response<GraphRowListModel> response = session.requestHandler().execute(graphRowListModelRequest)) {
                    return (Collection<T>) new GraphRowListModelMapper(session.metaData(), session.context())
                    .map(type, response);
                }
            } else {
                GraphModelRequest request = new DefaultGraphModelRequest(query.getStatement(), query.getParameters());
                try (Response<GraphModel> response = session.requestHandler().execute(request)) {
                    return (Collection<T>) new GraphEntityMapper(session.metaData(), session.context()).map(type, response);
                }
            }
        }, Transaction.Type.READ_WRITE);
    }

    public <T> Collection<T> loadAll(Class<T> type) {
        return loadAll(type, new Filters(), new SortOrder(), null, 1);
    }

    public <T> Collection<T> loadAll(Class<T> type, int depth) {
        return loadAll(type, new Filters(), new SortOrder(), null, depth);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filter filter) {
        return loadAll(type, new Filters().add(filter), new SortOrder(), null, 1);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filter filter, int depth) {
        return loadAll(type, new Filters().add(filter), new SortOrder(), null, depth);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder) {
        return loadAll(type, new Filters().add(filter), sortOrder, null, 1);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, int depth) {
        return loadAll(type, new Filters().add(filter), sortOrder, null, depth);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filter filter, Pagination pagination) {
        return loadAll(type, new Filters().add(filter), new SortOrder(), pagination, 1);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filter filter, Pagination pagination, int depth) {
        return loadAll(type, new Filters().add(filter), new SortOrder(), pagination, depth);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, Pagination pagination) {
        return loadAll(type, new Filters().add(filter), sortOrder, pagination, 1);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, Pagination pagination,
        int depth) {
        return loadAll(type, new Filters().add(filter), sortOrder, pagination, depth);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filters filters) {
        return loadAll(type, filters, new SortOrder(), null, 1);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filters filters, int depth) {
        return loadAll(type, filters, new SortOrder(), null, depth);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder) {
        return loadAll(type, filters, sortOrder, null, 1);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, int depth) {
        return loadAll(type, filters, sortOrder, null, depth);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filters filters, Pagination pagination) {
        return loadAll(type, filters, new SortOrder(), pagination, 1);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filters filters, Pagination pagination, int depth) {
        return loadAll(type, filters, new SortOrder(), pagination, depth);
    }

    public <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, Pagination pagination) {
        return loadAll(type, filters, sortOrder, pagination, 1);
    }

    public <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder) {
        return loadAll(type, new Filters(), sortOrder, null, 1);
    }

    public <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, int depth) {
        return loadAll(type, new Filters(), sortOrder, null, depth);
    }

    public <T> Collection<T> loadAll(Class<T> type, Pagination paging) {
        return loadAll(type, new Filters(), new SortOrder(), paging, 1);
    }

    public <T> Collection<T> loadAll(Class<T> type, Pagination paging, int depth) {
        return loadAll(type, new Filters(), new SortOrder(), paging, depth);
    }

    public <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, Pagination pagination) {
        return loadAll(type, new Filters(), sortOrder, pagination, 1);
    }

    public <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, Pagination pagination, int depth) {
        return loadAll(type, new Filters(), sortOrder, pagination, depth);
    }
}
