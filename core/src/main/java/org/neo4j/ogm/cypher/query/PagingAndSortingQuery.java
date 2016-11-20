/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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
 *
 * Only used by queries that return actual nodes and/or relationships from the graph. Other queries
 * just use {@link CypherQuery}
 *
 * @author Vince Bickers
 */
public class PagingAndSortingQuery extends CypherQuery implements PagingAndSorting {

    private Pagination paging;
    private SortOrder sortOrder = new SortOrder();

    protected PagingAndSortingQuery(String cypher, Map<String, ?> parameters) {
        super(cypher, parameters);
    }

    public String getStatement() {

        String stmt = statement.trim();
        String sorting = sortOrder().toString();
        String pagination = paging == null ? "" : page().toString();

        // these transformations are entirely dependent on the form of our base queries and
        // binding the sorting properties to the default query variables is a terrible hack. All this
        // needs refactoring ASAP.
        // Update Feb 2016: It really does need refactoring ASAP!!! //TODO
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
                    stmt = stmt.replace("RETURN p","RETURN p, ID(n)");
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
                    stmt = stmt.replace("RETURN p","RETURN p, ID(n)");
                }
            }
        }

        return stmt;

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

    public Pagination page() {
        return paging;
    }

    public SortOrder sortOrder() {
        return sortOrder;
    }

}
