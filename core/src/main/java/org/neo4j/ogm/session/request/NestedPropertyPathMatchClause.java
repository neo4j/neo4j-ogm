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

import org.neo4j.ogm.cypher.Filter;

/**
 * @author Gerrit Meier
 */
public class NestedPropertyPathMatchClause implements MatchClause {

    private int index;
    private String label;
    private final boolean isRelationship;
    private StringBuilder clause;

    NestedPropertyPathMatchClause(int index, String label, boolean isRelationship) {
        this.index = index;
        this.label = label;
        this.isRelationship = isRelationship;
        if (isRelationship) {
            clause = new StringBuilder(String.format("MATCH ()-[%s:`%s`]-() ", relationshipIdentifier(), this.label));
        } else {
            clause = new StringBuilder(String.format("MATCH (%s:`%s`) ", nodeIdentifier(), this.label));
        }
    }

    public String getLabel() {
        return label;
    }

    @Override
    public MatchClause append(Filter filter) {
        if (isRelationship) {
            clause.append(filter.toCypher(relationshipIdentifier(), clause.indexOf(" WHERE ") == -1));
        } else {
            clause.append(filter.toCypher(nodeIdentifier(), clause.indexOf(" WHERE ") == -1));
        }
        return this;
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
