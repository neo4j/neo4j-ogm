/*
 * Copyright (c) 2002-2019 "Neo4j,"
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

import static java.util.Objects.*;

import java.io.Serializable;
import java.util.Collection;

import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.session.request.FilteredQuery;
import org.neo4j.ogm.session.request.FilteredQueryBuilder;
import org.neo4j.ogm.session.request.strategy.LoadClauseBuilder;
import org.neo4j.ogm.session.request.strategy.MatchClauseBuilder;
import org.neo4j.ogm.session.request.strategy.QueryStatements;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Nicolas Mervaillie
 */
public class NodeQueryStatements<ID extends Serializable> implements QueryStatements<ID> {

    private String primaryIndex;

    private MatchClauseBuilder idMatchClauseBuilder = new IdMatchClauseBuilder();
    private MatchClauseBuilder idCollectionMatchClauseBuilder = new IdCollectionMatchClauseBuilder();
    private MatchClauseBuilder labelMatchClauseBuilder = new LabelMatchClauseBuilder();

    private LoadClauseBuilder loadClauseBuilder;

    // todo remove this constructor?
    public NodeQueryStatements() {
        loadClauseBuilder = new PathNodeLoadClauseBuilder();
    }

    public NodeQueryStatements(String primaryIndex, LoadClauseBuilder loadClauseBuilder) {
        this.primaryIndex = primaryIndex;
        this.loadClauseBuilder = requireNonNull(loadClauseBuilder);
    }

    @Override
    public PagingAndSortingQuery findOne(ID id, int depth) {
        return findOneByType("", id, depth);
    }

    @Override
    public PagingAndSortingQuery findOneByType(String label, ID id, int depth) {
        String matchClause;
        if (primaryIndex != null) {
            matchClause = idMatchClauseBuilder.build(label, primaryIndex);
        } else {
            matchClause = idMatchClauseBuilder.build(label);
        }
        String returnClause = loadClauseBuilder.build(label, depth);
        return new PagingAndSortingQuery(matchClause, returnClause, Utils.map("id", id), depth != 0, false);
    }

    @Override
    public PagingAndSortingQuery findAllByType(String label, Collection<ID> ids, int depth) {
        String matchClause;
        if (primaryIndex != null) {
            matchClause = idCollectionMatchClauseBuilder.build(label, primaryIndex);
        } else {
            matchClause = idCollectionMatchClauseBuilder.build(label);
        }
        String returnClause = loadClauseBuilder.build(label, depth);
        return new PagingAndSortingQuery(matchClause, returnClause, Utils.map("ids", ids), depth != 0, false);
    }

    @Override
    public PagingAndSortingQuery findByType(String label, int depth) {
        String matchClause = labelMatchClauseBuilder.build(label);
        String returnClause = loadClauseBuilder.build(label, depth);
        return new PagingAndSortingQuery(matchClause, returnClause, Utils.map(), depth != 0, false);
    }

    @Override
    public PagingAndSortingQuery findByType(String label, Filters parameters, int depth) {
        FilteredQuery filteredQuery = FilteredQueryBuilder.buildNodeQuery(label, parameters);
        String matchClause = filteredQuery.statement();
        String returnClause = loadClauseBuilder.build(label, depth);
        return new PagingAndSortingQuery(matchClause, returnClause, filteredQuery.parameters(), depth != 0, true);
    }

}
