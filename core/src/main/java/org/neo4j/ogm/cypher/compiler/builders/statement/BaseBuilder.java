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
package org.neo4j.ogm.cypher.compiler.builders.statement;

import org.neo4j.ogm.model.PropertyContainer;

/**
 * Base class for statements builders
 *
 * Provides common functionality to build statements.
 *
 * @author Frantisek Hartman
 */
abstract class BaseBuilder {

    /**
     * Appends optimistic locking check to statement
     *
     * This is actually a mixture of optimistic and pessimistic locking.
     *
     * @param sb builder where statement is build, expected open where clause (e.g. `WHERE ID(n)=row.nodeId`)
     * @param container node / relationship to check
     * @param variable
     */
    void appendVersionPropertyCheck(StringBuilder sb, PropertyContainer container, String variable) {
        // Will generate following:
        // AND n.`version` = {version} // Step 1
        // SET n.`version` = n.`version` + 1  // Step 2
        // WITH n WHERE n.version = {version} + 1 // Step 3

        // second version check is needed because what can happen is that another tx may happily update and increment
        // the version between step 1. and taking the lock in step 2

        String key = container.getVersion().getKey();
        sb.append("AND ")
            .append(variable).append(".`").append(key).append("` = row.`").append(key)
            .append("` SET ").append(variable).append(".`").append(key).append("` = ")
            .append(variable).append(".`").append(key).append("` + 1 WITH ").append(variable)
            .append(",row WHERE ").append(variable).append(".`").append(key)
            .append("` = row.`").append(key).append("` + 1 ");
    }
}
