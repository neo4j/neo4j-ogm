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
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
class NestedPathMatchClause implements MatchClause {

    private int index;
    private StringBuilder clause;

    NestedPathMatchClause(int index) {
        this(index, "n");
    }

    NestedPathMatchClause(int index, String varName) {

        this.index = index;
        this.clause = new StringBuilder("MATCH (").append(varName).append(")");
    }

    @Override
    public MatchClause append(Filter filter) {
        boolean wasPreviousSegmentRelationshipEntity = false;

        for (Filter.NestedPathSegment segment : filter.getNestedPath()) {
            boolean nestedRelationshipEntity = segment.isNestedRelationshipEntity();

            if (!wasPreviousSegmentRelationshipEntity) {
                if (segment.getRelationshipDirection() == Direction.INCOMING) {
                    clause.append("<");
                }

                clause.append(
                    String.format("-[%s:`%s`]-",
                        nestedRelationshipEntity && isLastSegment(filter, segment) ? "r" + index : "",
                        segment.getRelationshipType()));

                if (segment.getRelationshipDirection() == Direction.OUTGOING) {
                    clause.append(">");
                }
            }
            if (!nestedRelationshipEntity && !isLastSegment(filter, segment)) {
                clause.append(String.format("(:`%s`)", segment.getNestedEntityTypeLabel()));
            }
            wasPreviousSegmentRelationshipEntity = nestedRelationshipEntity;
        }

        clause.append(String.format("(%s) ", "m" + index));

        return this;
    }

    private boolean isLastSegment(Filter filter, Filter.NestedPathSegment segment) {
        return filter.getNestedPath().indexOf(segment) == filter.getNestedPath().size() - 1;
    }

    @Override
    public String toCypher() {
        return clause.toString();
    }
}
