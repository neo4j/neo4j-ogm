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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.exception.core.MissingOperatorException;

/**
 * @author Jasper Blues
 * @author Michael J. Simons
 */
public class NodeQueryBuilder {

    private final String varName;
    private PrincipalNodeMatchClause principalClause;
    private Iterable<Filter> filters;
    private List<MatchClause> nestedClauses;
    private List<MatchClause> pathClauses;
    private Map<String, Object> parameters;
    private int matchClauseId;
    private boolean built = false;
    private boolean hasRelationshipMatch = false;

    NodeQueryBuilder(String principalLabel, Iterable<Filter> filters) {
        this(principalLabel, filters, "n");
    }

    NodeQueryBuilder(String principalLabel, Iterable<Filter> filters, String varName) {
        this.varName = varName;
        this.principalClause = principalLabel == null ? null : new PrincipalNodeMatchClause(principalLabel, varName);
        this.filters = filters;
        this.nestedClauses = new ArrayList<>();
        this.pathClauses = new ArrayList<>();
        this.parameters = new HashMap<>();
        this.matchClauseId = 0;
        this.built = false;
    }

    public FilteredQuery build() {
        if (!built) {
            int i = 0;
            for (Filter filter : filters) {
                if (i != 0 && filter.getBooleanOperator().equals(BooleanOperator.NONE)) {
                    throw new MissingOperatorException("BooleanOperator missing for filter with property name "
                        + filter.getPropertyName() + ". Only the first filter may not specify the BooleanOperator.");
                }
                if (filter.isNested()) {
                    appendNestedFilter(filter);
                    hasRelationshipMatch = true;
                } else if (filter.isDeepNested()) {
                    appendDeepNestedFilter(filter);
                    hasRelationshipMatch = true;
                } else if (principalClause != null) {
                    //If the filter is not nested, it belongs to the node we're returning
                    principalClause().append(filter);
                }
                parameters.putAll(filter.parameters());
                i++;
            }
            built = true;
        }
        return new FilteredQuery(toCypher(), parameters);
    }

    private void appendNestedFilter(Filter filter) {
        if (filter.isNestedRelationshipEntity()) {
            MatchClause clause = findExistingNestedClause(RelationshipPropertyMatchClause.class,
                c -> c.getRelationshipType().equals(filter.getRelationshipType()));
            if (clause == null) {
                clause = new RelationshipPropertyMatchClause(matchClauseId, filter.getRelationshipType());
                nestedClauses.add(clause);
            }
            clause.append(filter);
        } else {
            MatchClause clause = findExistingNestedClause(RelatedNodePropertyMatchClause.class,
                c -> c.getLabel().equals(filter.getNestedEntityTypeLabel()) && c.getProperty().equals(filter.getNestedPropertyName()));
            if (clause == null) {
                clause = new RelatedNodePropertyMatchClause(filter.getNestedEntityTypeLabel(), filter.getNestedPropertyName(), matchClauseId);
                nestedClauses.add(clause);
                pathClauses.add(new PathMatchClause(matchClauseId).append(filter));
            }
            clause.append(filter);
        }
        matchClauseId++;
    }

    private void appendDeepNestedFilter(Filter filter) {
        Filter.NestedPathSegment lastPathSegment = filter.getNestedPath().get(filter.getNestedPath().size() - 1);
        MatchClause clause;
        if (lastPathSegment.isNestedRelationshipEntity()) {
            clause = findExistingNestedClause(NestedPropertyPathMatchClause.class,
                c -> c.getLabel().equals(lastPathSegment.getRelationshipType()));
        } else {
            clause = findExistingNestedClause(NestedPropertyPathMatchClause.class,
                c -> c.getLabel().equals(lastPathSegment.getNestedEntityTypeLabel()) && c.getProperty().equals(lastPathSegment.getPropertyName()));
        }
        if (clause == null) {
            clause = new NestedPropertyPathMatchClause(matchClauseId,
                lastPathSegment.getNestedEntityTypeLabel(), lastPathSegment.getPropertyName(), lastPathSegment.isNestedRelationshipEntity());
            nestedClauses.add(clause);
        }
        pathClauses.add(new NestedPathMatchClause(matchClauseId, this.varName).append(filter));
        clause.append(filter);

        matchClauseId++;
    }

    private PrincipalNodeMatchClause principalClause() {
        return principalClause;
    }

    private <T> T findExistingNestedClause(Class<T> targetClass, Predicate<T> predicate) {
        for (MatchClause clause : nestedClauses) {
            if (targetClass.isInstance(clause)) {
                T nestedPropClause = (T) clause;
                if (predicate.test(nestedPropClause)) {
                    return nestedPropClause;
                }
            }
        }
        return null;
    }

    private StringBuilder toCypher() {
        StringBuilder stringBuilder = new StringBuilder();

        if (principalClause != null) {
            stringBuilder.append(principalClause.toCypher());
        }

        for (MatchClause matchClause : nestedClauses) {
            stringBuilder.append(matchClause.toCypher());
        }

        for (MatchClause matchClause : pathClauses) {
            stringBuilder.append(matchClause.toCypher());
        }

        if (hasRelationshipMatch) {
            stringBuilder.append("WITH DISTINCT ");
        } else {
            stringBuilder.append("WITH ");
        }
        stringBuilder.append(this.varName);

        return stringBuilder;
    }
}
