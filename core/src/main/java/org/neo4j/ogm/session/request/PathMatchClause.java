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

import org.neo4j.ogm.annotation.Relationship.Direction;
import org.neo4j.ogm.cypher.Filter;

/**
 * There is one PathMatchClause per set (one or more) RelatedNodePropertyMatchClause
 *
 * @author Jasper Blues
 */
public class PathMatchClause implements MatchClause {

    private final int index;
    private final StringBuilder clause;

    PathMatchClause(int index) {
        this.index = index;
        this.clause = new StringBuilder("MATCH (n)");
    }

    @Override
    public MatchClause append(Filter filter) {
        if (filter.getRelationshipDirection() == Direction.INCOMING) {
            clause.append("<");
        }

        clause.append(
            String.format("-[%s:`%s`]-", filter.isNestedRelationshipEntity() ? "r" : "", filter.getRelationshipType())
        );

        if (filter.getRelationshipDirection() == Direction.OUTGOING) {
            clause.append(">");
        }

        clause.append(String.format("(%s) ", "m" + index));
        return this;
    }

    @Override
    public String toCypher() {
        return clause.toString();
    }
}
