/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

    private Pagination paging;
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
        if (paging != null) {
            sb.append(paging.toString());
        }
        sb.append(this.returnClause);
        if (needsRowResult()) {
            sb.append(", ID(").append(variable).append(")");
        }
        return sb.toString();
    }

    public boolean needsRowResult() {
        return (sortOrder.hasSortClauses() || (paging != null) || hasPredicate) && returnsPath;
    }

    @Override
    public PagingAndSortingQuery setPagination(Pagination paging) {
        this.paging = paging;
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

    private Pagination page() {
        return paging;
    }

    private SortOrder sortOrder() {
        return sortOrder;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
