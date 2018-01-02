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

package org.neo4j.ogm.session.request;

import java.util.Map;

/**
 * A FilteredQuery is constructed by a {@link FilteredQueryBuilder}
 * A FilteredQuery represents the MATCH clause of a cypher statement containing various WHERE predicates
 * The various implementing classes of {@link org.neo4j.ogm.session.request.strategy.DeleteStatements},
 * {@link org.neo4j.ogm.session.request.strategy.AggregateStatements} and
 * {@link org.neo4j.ogm.session.request.strategy.QueryStatements} can set the return clause that
 * fits their purpose.
 *
 * @author vince
 */
public class FilteredQuery {

    private final StringBuilder stringBuilder;
    private final Map<String, Object> parameters;
    private String returnClause = "";

    FilteredQuery(StringBuilder stringBuilder, Map<String, Object> parameters) {
        this.stringBuilder = stringBuilder;
        this.parameters = parameters;
    }

    /**
     * Set the return clause to be used with the query body. The same query body can
     * be re-used in different contexts - fetch, delete, count and so on.
     *
     * @param returnClause the return clause to be used with this query body
     */
    public void setReturnClause(String returnClause) {
        this.returnClause = returnClause;
    }

    /**
     * @return Cypher consisting of the query body to which the return clause (if any)
     * is appended.
     */
    public String statement() {
        return stringBuilder.toString().concat(returnClause);
    }

    /**
     * @return the parameters to be used with this query
     */
    public Map<String, Object> parameters() {
        return parameters;
    }
}

