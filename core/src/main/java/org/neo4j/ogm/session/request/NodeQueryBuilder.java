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
package org.neo4j.ogm.session.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.exception.core.MissingOperatorException;

/**
 * @author Jasper Blues
 */
public class NodeQueryBuilder {

    private PrincipalNodeMatchClause principalClause;
    private Iterable<Filter> filters;
    private List<MatchClause> nestedClauses;
    private List<MatchClause> pathClauses;
    private Map<String, Object> parameters;
    private int matchClauseId;
    private boolean built = false;
    private boolean hasRelationshipMatch = false;

    NodeQueryBuilder(String principalLabel, Iterable<Filter> filters) {
        this.principalClause = new PrincipalNodeMatchClause(principalLabel);
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
                } else {
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
        if (filter.getBooleanOperator().equals(BooleanOperator.OR)) {
            throw new UnsupportedOperationException("OR is not supported for nested properties on an entity");
        }
        if (filter.isNestedRelationshipEntity()) {
            MatchClause clause = relationshipPropertyClauseFor(filter.getRelationshipType());
            if (clause == null) {
                clause = new RelationshipPropertyMatchClause(matchClauseId, filter.getRelationshipType());
                nestedClauses.add(clause);
            }
            clause.append(filter);
        } else {
            MatchClause clause = relatedNodeClauseFor(filter.getNestedEntityTypeLabel());
            if (clause == null) {
                clause = new RelatedNodePropertyMatchClause(filter.getNestedEntityTypeLabel(), matchClauseId);
                nestedClauses.add(clause);
                pathClauses.add(new PathMatchClause(matchClauseId).append(filter));
            }
            clause.append(filter);
        }
        matchClauseId++;
    }

    private void appendDeepNestedFilter(Filter filter) {
        if (filter.getBooleanOperator().equals(BooleanOperator.OR)) {
            throw new UnsupportedOperationException("OR is not supported for nested properties on an entity");
        }
        Filter.NestedPathSegment lastPathSegment = filter.getNestedPath().get(filter.getNestedPath().size() - 1);
        MatchClause clause = new NestedPropertyPathMatchClause(matchClauseId, lastPathSegment.getNestedEntityTypeLabel(), lastPathSegment.isNestedRelationshipEntity());

        pathClauses.add(new NestedPathMatchClause(matchClauseId).append(filter));
        nestedClauses.add(clause);
        clause.append(filter);

        matchClauseId++;
    }

    private PrincipalNodeMatchClause principalClause() {
        return principalClause;
    }

    private RelatedNodePropertyMatchClause relatedNodeClauseFor(String label) {
        for (MatchClause clause : nestedClauses) {
            if (clause instanceof RelatedNodePropertyMatchClause) {
                RelatedNodePropertyMatchClause nestedPropClause = (RelatedNodePropertyMatchClause) clause;
                if (nestedPropClause.getLabel().equals(label)) {
                    return nestedPropClause;
                }
            }
        }
        return null;
    }

    private RelationshipPropertyMatchClause relationshipPropertyClauseFor(String relationshipType) {
        for (MatchClause clause : nestedClauses) {
            if (clause instanceof RelationshipPropertyMatchClause) {
                RelationshipPropertyMatchClause relPropClause = (RelationshipPropertyMatchClause) clause;
                if (relPropClause.getRelationshipType().equals(relationshipType)) {
                    return relPropClause;
                }
            }
        }
        return null;
    }

    private StringBuilder toCypher() {
        StringBuilder stringBuilder = new StringBuilder(principalClause.toCypher());

        for (MatchClause matchClause : nestedClauses) {
            stringBuilder.append(matchClause.toCypher());
        }

        for (MatchClause matchClause : pathClauses) {
            stringBuilder.append(matchClause.toCypher());
        }

        if (hasRelationshipMatch) {
            stringBuilder.append("WITH DISTINCT n");
        } else {
            stringBuilder.append("WITH n");
        }

        return stringBuilder;
    }
}
