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

