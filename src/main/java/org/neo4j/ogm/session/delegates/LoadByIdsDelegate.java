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
package org.neo4j.ogm.session.delegates;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.Query;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.session.Capability;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.request.strategy.QueryStatements;
import org.neo4j.ogm.session.response.Neo4jResponse;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Vince Bickers
 */
public class LoadByIdsDelegate implements Capability.LoadByIds {


    private final Neo4jSession session;

    public LoadByIdsDelegate(Neo4jSession session) {
        this.session = session;
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, SortOrder sortOrder, Pagination pagination, int depth) {

        String url = session.ensureTransaction().url();
        String entityType = session.entityType(type.getName());
        QueryStatements queryStatements = session.queryStatementsFor(type);

        Query qry = queryStatements.findAllByType(entityType, ids, depth)
                .setSortOrder(sortOrder)
                .setPagination(pagination);

        try (Neo4jResponse<GraphModel> response = session.requestHandler().execute(qry, url)) {
            session.responseHandler().loadAll(type, response);
            return lookup(type, ids);
        }
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids) {
        return loadAll(type, ids, new SortOrder(), null, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, int depth) {
        return loadAll(type, ids, new SortOrder(), null, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, SortOrder sortOrder) {
        return loadAll(type, ids, sortOrder, null, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, SortOrder sortOrder, int depth) {
        return loadAll(type, ids, sortOrder, null, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, Pagination paging) {
        return loadAll(type, ids, new SortOrder(), paging, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, Pagination paging, int depth) {
        return loadAll(type, ids, new SortOrder(), paging, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, SortOrder sortOrder, Pagination pagination) {
        return loadAll(type, ids, sortOrder, pagination, 1);
    }

    private <T> Collection<T> lookup(Class<T> type, Collection<Long> ids) {

        Set<T> results = new HashSet<T>();
        ClassInfo typeInfo = session.metaData().classInfo(type.getName());

        for (Long id : ids) {

            Object ref;

            if (typeInfo.annotationsInfo().get(RelationshipEntity.CLASS) == null) {
                ref = session.context().getNodeEntity(id);
            } else {
                ref = session.context().getRelationshipEntity(id);
            }
            try {
                results.add(type.cast(ref));
            } catch (ClassCastException cce) {
                // do nothing, the object is not loadable in the domain;
            }
        }
        return results;
    }

}
