/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.cypher.statement;

import org.apache.commons.lang.StringUtils;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.cypher.statement.parser.Clause;
import org.neo4j.ogm.cypher.statement.parser.MatchClause;
import org.neo4j.ogm.cypher.statement.parser.StatementParser;
import org.neo4j.ogm.cypher.statement.parser.WithClause;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

/**
 * Simple encapsulation of a Cypher query and its parameters and other optional parts (paging/sort).
 *
 * Note, this object will be transformed directly to JSON so don't add anything here that is
 * not part of the HTTP Transactional endpoint syntax
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Rene Richter
 */
public class ParameterisedStatement {

    private String statement;


    private Map<String, Object> parameters = new HashMap<>();
    private String[] resultDataContents;
    private boolean includeStats = false;

    private Pagination paging;
    private SortOrder sortOrder;
    private Filters filters = new Filters();


    /**
     * Constructs a new {@link ParameterisedStatement} based on the given Cypher query string and query parameters.
     *
     * @param cypher The parameterised Cypher query string
     * @param parameters The name-value pairs that satisfy the parameters in the given query
     */
    public ParameterisedStatement(String cypher, Map<String, ?> parameters) {
        this(cypher, parameters, "row");
    }

    protected ParameterisedStatement(String cypher, Map<String, ?> parameters, String... resultDataContents) {
        this.statement = cypher;
        this.parameters.putAll(parameters);
        this.resultDataContents = resultDataContents;

    }

    protected ParameterisedStatement(String cypher, Map<String, ?> parameters, boolean includeStats, String... resultDataContents) {
        this.statement = cypher;
        this.parameters.putAll(parameters);
        this.resultDataContents = resultDataContents;
        this.includeStats = includeStats;
    }

    /**
     * Returns the statement.
     *
     * If paging or sorting are available, the statement will be augmented.
     * @return The statement.
     */
    public String getStatement() {
        String stmt = statement.trim();
        if (this.sortOrder != null || this.paging != null) {
            stmt = augmentStatementWithSortingAndPagination(stmt);
        }
        return stmt;
    }

    private String augmentStatementWithSortingAndPagination(String stmt) {
        StatementParser parser = new StatementParser();
        LinkedList<Clause> clauses = parser.parseStatement(stmt);
        ListIterator<Clause> clausesIterator = clauses.listIterator(clauses.size()-1);

        Clause returnClause = clauses.getLast();
        Clause clauseBeforeReturn = clausesIterator.previous();

        String pagination = paging == null ? "" : page().toString();
        //by default, sorting gets resolved by return-clause.
        String sorting = resolveSortOrder(returnClause.getAliases().get(0));

        if(clauseBeforeReturn instanceof MatchClause
                && ((MatchClause)clauseBeforeReturn).isPathMatch()) {
            //so, if we are here, it is a match-clause for a path
            if(clausesIterator.hasPrevious()) {
                Clause previousWithClause = clausesIterator.previous();
                //sorting gets now resolved by with-clause.
                sorting = resolveSortOrder(previousWithClause.getAliases().get(0));
                return stmt.replace(previousWithClause.getContent()
                        ,previousWithClause.getContent()+sorting+pagination);
            } else {
                //if no with-clause is present, sorting gets resolved by match-clause.
                sorting = resolveSortOrder(clauseBeforeReturn.getAliases().get(0));
            }
        }

        if(clauseBeforeReturn instanceof WithClause) {
            String withClause = clauseBeforeReturn.getContent();
            return stmt.replace(withClause,withClause+sorting+pagination);
        }


        String withAliases = StringUtils.join(clauseBeforeReturn.getAliases(),',');
        return stmt.replace("RETURN "
                ,"WITH "+withAliases+sorting+pagination+" RETURN ");
    }

    private String resolveSortOrder(String substitute) {
        return sortOrder == null
                ? ""
                : sortOrder().toString().replace("$", substitute);
    }


    public Map<String, Object> getParameters() {
        return parameters;
    }

    public String[] getResultDataContents() {
        return resultDataContents;
    }

    public boolean isIncludeStats() {
        return includeStats;
    }

    public Pagination page() {
        return paging;
    }

    public SortOrder sortOrder() {
        return sortOrder;
    }

    protected void addPaging(Pagination page) {
        this.paging = page;
    }

    public void addSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void addFilters(Filters filters) {
        this.filters = filters;
    }


}

