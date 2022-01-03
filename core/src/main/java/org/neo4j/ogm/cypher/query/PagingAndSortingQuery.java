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
package org.neo4j.ogm.cypher.query;

import java.util.Map;

/**
 * Extends {@link CypherQuery} with additional functionality for Paging and Sorting.
 * Only used by queries that return actual nodes and/or relationships from the graph. Other queries
 * just use {@link CypherQuery}
 *
 * @author Vince Bickers
 */
public class PagingAndSortingQuery implements PagingAndSorting {

    private Pagination pagination;
    private SortOrder sortOrder = new SortOrder();

    private String matchClause;
    private String returnClause;

    private final Map<String, Object> parameters;

    private boolean returnsPath = false;
    private boolean hasPredicate;

    private String variable;

    public PagingAndSortingQuery(String matchClause, String returnClause, Map<String, Object> parameters,
        boolean returnsPath,
        boolean hasPredicate) {
        this(matchClause, returnClause, parameters, returnsPath, hasPredicate, "n");
    }

    public PagingAndSortingQuery(
        String matchClause, String returnClause, Map<String, Object> parameters,
        boolean returnsPath,
        boolean hasPredicate, String variable) {
        this.matchClause = matchClause;
        this.returnClause = returnClause;
        this.parameters = parameters;
        this.returnsPath = returnsPath;
        this.hasPredicate = hasPredicate;
        this.variable = variable;
    }

    public String getStatement() {
        String sorting = sortOrder().asString();

        StringBuilder sb = new StringBuilder();
        sb.append(matchClause);

        if (!sorting.isEmpty()) {
            sb.append(sorting.replace("$", variable));
        }
        if (pagination != null) {
            sb.append(pagination.toString());
        }
        sb.append(this.returnClause);
        if (needsRowResult()) {
            sb.append(", ID(").append(variable).append(")");
        }
        return sb.toString();
    }

    public boolean needsRowResult() {
        return (sortOrder.hasSortClauses() || (pagination != null) || hasPredicate) && returnsPath;
    }

    @Override
    public PagingAndSortingQuery setPagination(Pagination pagination) {
        this.pagination = pagination;
        return this;
    }

    @Override
    public PagingAndSortingQuery setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public void setReturnsPath(boolean returnsPath) {
        this.returnsPath = returnsPath;
    }

    private SortOrder sortOrder() {
        return sortOrder;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
