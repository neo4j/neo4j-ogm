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

package org.neo4j.ogm.session.request;

import org.neo4j.ogm.annotation.Relationship;
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
            if (filter.getRelationshipDirection().equals(Relationship.INCOMING)) {
                clause.append("<");
            }
            //			String relationshipIdentifier = filter.isNestedRelationshipEntity() ? relationshipIdentifier() : "";
            clause.append(String.format("-[%s:`%s`]-", relationshipIdentifier(), this.relationshipType));
            if (filter.getRelationshipDirection().equals(Relationship.OUTGOING)) {
                clause.append(">");
            }
            clause.append(String.format("(%s) ", nodeIdentifier()));
        }

        //TODO this implies support for querying by one relationship entity only
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
