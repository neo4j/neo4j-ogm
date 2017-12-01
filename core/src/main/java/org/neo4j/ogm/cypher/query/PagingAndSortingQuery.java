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

    private String statement;
    private String matchClause;
    private String returnClause;

    private final Map<String, Object> parameters;
    protected int withIndex;

    private boolean returnsPath = false;
    private boolean hasPredicate;

    public PagingAndSortingQuery(String statement, Map<String, Object> parameters) {
        this.statement = statement;
        this.parameters = parameters;
        this.withIndex = this.statement.indexOf("WITH n");
        if (this.withIndex == -1) {
            int withIndex = this.statement.indexOf("WITH DISTINCT(r");
            if (withIndex == -1) {
                withIndex = this.statement.indexOf("WITH r");
            }
            this.withIndex = withIndex;
        }
        hasPredicate = statement.contains("WHERE");
        returnsPath = statement.matches(".*RETURN.*p.*");
    }

    public PagingAndSortingQuery(String matchClause, String returnClause, Map<String, Object> parameters,
                                 boolean returnsPath,
                                 boolean hasPredicate) {
        this.matchClause = matchClause;
        this.returnClause = returnClause;
        this.parameters = parameters;
        this.returnsPath = returnsPath;
        this.hasPredicate = hasPredicate;
    }


    public String getStatement() {
        String sorting = sortOrder().toString();
        if (statement == null) {

            StringBuilder sb = new StringBuilder();

            String returnClause = this.returnClause;

            sb.append(matchClause);
            if (!sorting.isEmpty()) {
                sb.append(sorting.replace("$", "n"));
            }
            if (paging != null) {
                sb.append(paging.toString());
            }
            sb.append(returnClause);
            if (needsRowResult()) {
                sb.append(", ID(n)");
            }
            return sb.toString();

        }


        // only used for relationship entity queries now, remove when relationship entity queries moved to new query building

        String stmt = statement.trim();
        String pagination = paging == null ? "" : page().toString();

        // these transformations are entirely dependent on the form of our base queries and
        // binding the sorting properties to the default query variables is a terrible hack. All this
        // needs refactoring ASAP.
        // Update Feb 2017: It really does need refactoring ASAP!!! //TODO
        if (sorting.length() > 0 || pagination.length() > 0) {

            if (withIndex > -1) {
                int nextClauseIndex = stmt.indexOf(" MATCH", withIndex);
                String withClause = stmt.substring(withIndex, nextClauseIndex);
                String newWithClause = withClause;
                if (stmt.contains(")-[r0")) {
                    sorting = sorting.replace("$", "r0");
                    if (!withClause.contains(",r0") && (!withClause.contains("r0,"))) {
                        newWithClause = newWithClause + ",r0";
                    }
                } else {
                    sorting = sorting.replace("$", "n");
                }
                stmt = stmt.replace(withClause, newWithClause + sorting + pagination);
                //If a path is returned, also return the original entities in the page
                if (stmt.contains("MATCH p=(") && !stmt.contains("RETURN p, ID(n)")) {
                    stmt = stmt.replace("RETURN p", "RETURN p, ID(n)");
                }
            } else {
                if (stmt.startsWith("MATCH p=(")) {
                    String withClause = "WITH p";
                    if (stmt.contains(")-[r")) {
                        withClause = withClause + ",r0";
                        sorting = sorting.replace("$", "r0");
                    } else {
                        sorting = sorting.replace("$", "n");
                    }
                    stmt = stmt.replace("RETURN ", withClause + sorting + pagination + " RETURN ");
                } else {
                    sorting = sorting.replace("$", "n");
                    stmt = stmt.replace("RETURN ", "WITH n" + sorting + pagination + " RETURN ");
                }
                if (stmt.contains("MATCH p=(") && stmt.contains("WITH n") && !stmt.contains("RETURN p, ID(n)")) {
                    stmt = stmt.replace("RETURN p", "RETURN p, ID(n)");
                }
            }
        }

        return stmt;
    }

    public boolean needsRowResult() {
        return ((sortOrder.toString().length() > 0) || (paging != null) || hasPredicate) && returnsPath;
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
