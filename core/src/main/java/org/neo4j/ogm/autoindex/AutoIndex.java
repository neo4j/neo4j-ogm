/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
