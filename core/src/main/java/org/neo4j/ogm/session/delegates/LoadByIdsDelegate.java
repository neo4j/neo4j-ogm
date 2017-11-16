/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.ogm.context.GraphEntityMapper;
import org.neo4j.ogm.cypher.query.DefaultGraphModelRequest;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.request.GraphModelRequest;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.request.strategy.QueryStatements;
import org.neo4j.ogm.utils.EntityUtils;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class LoadByIdsDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(LoadByIdsDelegate.class);
    private final Neo4jSession session;

    public LoadByIdsDelegate(Neo4jSession session) {
        this.session = session;
    }

    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder, Pagination pagination, int depth) {

        String entityLabel = session.entityType(type.getName());
        if (entityLabel == null) {
            LOG.warn("Unable to find database label for entity " + type.getName()
                + " : no results will be returned. Make sure the class is registered, "
                + "and not abstract without @NodeEntity annotation");
        }
        QueryStatements<ID> queryStatements = session.queryStatementsFor(type, depth);

        PagingAndSortingQuery qry = queryStatements.findAllByType(entityLabel, ids, depth)
                .setSortOrder(sortOrder)
                .setPagination(pagination);

        GraphModelRequest request = new DefaultGraphModelRequest(qry.getStatement(), qry.getParameters());
        try (Response<GraphModel> response = session.requestHandler().execute(request)) {
            Iterable<T> mapped = new GraphEntityMapper(session.metaData(), session.context()).map(type, response);

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
    }

    private <T, ID extends Serializable> Set<T> sortResultsByIds(Class<T> type, Collection<ID> ids, Iterable<T> mapped) {
        Map<ID, T> items = new HashMap<>();
        ClassInfo classInfo = session.metaData().classInfo(type.getName());

        FieldInfo idField = classInfo.primaryIndexField();
        if (idField == null) {
            idField = classInfo.identityField();
        }

        for (T t : mapped) {
            Object id = idField.read(t);
            if (id != null) {
                items.put((ID) id, t);
            }
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

    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder, int depth) {
        return loadAll(type, ids, sortOrder, null, depth);
    }

    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, Pagination paging) {
        return loadAll(type, ids, new SortOrder(), paging, 1);
    }

    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, Pagination paging, int depth) {
        return loadAll(type, ids, new SortOrder(), paging, depth);
    }

    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder, Pagination pagination) {
        return loadAll(type, ids, sortOrder, pagination, 1);
    }

    private <T, ID extends Serializable> boolean includeMappedEntity(Collection<ID> ids, T mapped) {

        final ClassInfo classInfo = session.metaData().classInfo(mapped);
        final FieldInfo primaryIndexField = classInfo.primaryIndexField();

        if (primaryIndexField != null) {
            final Object primaryIndexValue = primaryIndexField.read(mapped);
            if (ids.contains(primaryIndexValue)) {
                return true;
            }
        }
        Object id = EntityUtils.identity(mapped, session.metaData());
        return ids.contains(id);
    }
}
