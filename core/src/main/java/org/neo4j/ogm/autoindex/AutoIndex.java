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
package org.neo4j.ogm.autoindex;

import java.util.Collections;
import java.util.Map;

import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.session.request.RowDataStatement;

/**
 * Represents an Index that can be auto generated in Neo4j.
 *
 * @author Mark Angrish
 * @author Eric Spiegelberg
 */
class AutoIndex {

    private static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();

    /**
     * This is a cypher fragment that is used during create and drop for constraints.
     */
    private final String description;

    AutoIndex(String label, String property, boolean unique) {

        if (unique) {
            this.description =
                "CONSTRAINT ON ( `" + label.toLowerCase() + "`:`" + label + "` ) ASSERT `" + label.toLowerCase() + "`.`"
                    + property + "` IS UNIQUE";
        } else {
            this.description = "INDEX ON :`" + label + "`( `" + property + "` )";
        }
    }

    Statement getCreateStatement() {
        return new RowDataStatement("CREATE " + this.description, EMPTY_MAP);
    }

    public Statement getDropStatement() {
        return new RowDataStatement("DROP " + this.description, EMPTY_MAP);
    }

    String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AutoIndex autoIndex = (AutoIndex) o;

        return description != null ? description.equals(autoIndex.description) : autoIndex.description == null;
    }

    @Override
    public int hashCode() {
        return description != null ? description.hashCode() : 0;
    }
}
