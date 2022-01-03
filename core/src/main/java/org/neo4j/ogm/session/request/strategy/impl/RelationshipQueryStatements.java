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
package org.neo4j.ogm.session.request.strategy.impl;

import static java.util.Collections.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.exception.core.InvalidDepthException;
import org.neo4j.ogm.session.request.FilteredQuery;
import org.neo4j.ogm.session.request.FilteredQueryBuilder;
import org.neo4j.ogm.session.request.strategy.LoadClauseBuilder;
import org.neo4j.ogm.session.request.strategy.MatchClauseBuilder;
import org.neo4j.ogm.session.request.strategy.QueryStatements;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class RelationshipQueryStatements<ID extends Serializable> implements QueryStatements<ID> {

    private MatchClauseBuilder idMatchClauseBuilder = new IdMatchRelationshipClauseBuilder();
    private MatchClauseBuilder idCollectionMatchClauseBuilder = new IdCollectionMatchRelationshipClauseBuilder();
    private MatchClauseBuilder relTypeMatchClauseBuilder = new RelationshipTypeMatchClauseBuilder();

    private LoadClauseBuilder loadClauseBuilder;
    private String primaryId;

    public RelationshipQueryStatements() {
        loadClauseBuilder = new PathRelationshipLoadClauseBuilder();
    }

    public RelationshipQueryStatements(String primaryId, LoadClauseBuilder loadClauseBuilder) {
        this.primaryId = primaryId;
        this.loadClauseBuilder = loadClauseBuilder;
    }

    @Override
    public PagingAndSortingQuery findOne(ID id, int depth) {
        if (depth > 0) {
            String matchClause = idMatchClauseBuilder.build("");
            String returnClause = loadClauseBuilder.build("r0", "", depth);
            return new PagingAndSortingQuery(matchClause, returnClause, Collections.singletonMap("id", id), true, true, "r0");
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }

    @Override
    public PagingAndSortingQuery findOneByType(String label, ID id, int depth) {
        if (label == null || label.equals("")) {
            throw new IllegalArgumentException("no label provided");
        }

        if (depth > 0) {
            String matchClause;
            if (primaryId == null) {
                matchClause = idMatchClauseBuilder.build(label);
            } else {
                matchClause = idMatchClauseBuilder.build(label, primaryId);
            }
            String returnClause = loadClauseBuilder.build("r0", label, depth);
            return new PagingAndSortingQuery(matchClause, returnClause, Collections.singletonMap("id", id), true, true, "r0");
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }

    @Override
    public PagingAndSortingQuery findAllByType(String type, Collection<ID> ids, int depth) {
        if (depth > 0) {
            String matchClause;
            if (primaryId == null) {
                matchClause = idCollectionMatchClauseBuilder.build(type);
            } else {
                matchClause = idCollectionMatchClauseBuilder.build(type, primaryId);
            }
            String loadClause = loadClauseBuilder.build("r0", type, depth);
            return new PagingAndSortingQuery(matchClause, loadClause, Collections.singletonMap("ids", ids), true, true, "r0");
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }

    @Override
    public PagingAndSortingQuery findByType(String type, int depth) {
        if (depth > 0) {
            String matchClause = relTypeMatchClauseBuilder.build(type);
            String loadClause = loadClauseBuilder.build("r0", type, depth);
            return new PagingAndSortingQuery(matchClause, loadClause, emptyMap(), true, true, "r0");
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }

    @Override
    public PagingAndSortingQuery findByType(String type, Filters parameters, int depth) {
        if (depth > 0) {
            FilteredQuery query = FilteredQueryBuilder.buildRelationshipQuery(type, parameters);
            String matchClause = query.statement() + " WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m";
            String returnClause = loadClauseBuilder.build("r0", type, depth);
            return new PagingAndSortingQuery(matchClause, returnClause, query.parameters(), true, true, "r0");
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }

    private int min(int depth) {
        return Math.min(0, depth);
    }

    private int max(int depth) {
        return Math.max(0, depth);
    }
}
