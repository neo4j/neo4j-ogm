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
 * @author Jasper Blues
 */
public class RelationshipPropertyMatchClause implements MatchClause {

    private int index;
    private String relationshipType;
    private StringBuilder clause;

    public RelationshipPropertyMatchClause(int index, String relationshipType) {
        this.index = index;
        this.relationshipType = relationshipType;
    }

    @Override
    public MatchClause append(Filter filter) {
        if (clause == null) {
            clause = new StringBuilder("MATCH (n)");
            if (filter.getRelationshipDirection() == Direction.INCOMING) {
                clause.append("<");
            }
            // String relationshipIdentifier = filter.isNestedRelationshipEntity() ? relationshipIdentifier() : "";
            clause.append(String.format("-[%s:`%s`]-", relationshipIdentifier(), this.relationshipType));
            if (filter.getRelationshipDirection() == Direction.OUTGOING) {
                clause.append(">");
            }
            clause.append(String.format("(%s) ", nodeIdentifier()));
        }

        // TODO this implies support for querying by one relationship entity only
        clause.append(filter.toCypher(relationshipIdentifier(), clause.indexOf(" WHERE ") == -1));
        return this;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    @Override
    public String toCypher() {
        return clause.toString();
    }

    private String nodeIdentifier() {
        return "m" + this.index;
    }

    private String relationshipIdentifier() {
        return "r" + this.index;
    }
}
