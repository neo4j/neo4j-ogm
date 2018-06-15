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

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.cypher.Filter;

/**
 * There is one PathMatchClause per set (one or more) RelatedNodePropertyMatchClause
 *
 * @author Jasper Blues
 */
public class PathMatchClause implements MatchClause {

    private int index;
    private StringBuilder clause;

    PathMatchClause(int index) {
        this.index = index;
        this.clause = new StringBuilder("MATCH (n)");
    }

    @Override
    public MatchClause append(Filter filter) {
        if (filter.getRelationshipDirection().equals(Relationship.INCOMING)) {
            clause.append("<");
        }

        clause.append(
            String.format("-[%s:`%s`]-", filter.isNestedRelationshipEntity() ? "r" : "", filter.getRelationshipType())
        );

        if (filter.getRelationshipDirection().equals(Relationship.OUTGOING)) {
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
