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
package org.neo4j.ogm.session.request.strategy.impl;

import org.neo4j.ogm.session.request.strategy.MatchClauseBuilder;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class IdMatchRelationshipClauseBuilder implements MatchClauseBuilder {

    @Override
    public String build(String label) {
        if (label == null || label.isEmpty()) {
            return "MATCH ()-[r0]->() WHERE ID(r0)=$id  WITH r0, STARTNODE(r0) AS n, ENDNODE(r0) AS m";
        } else {
            return "MATCH ()-[r0:`" + label + "`]->() WHERE ID(r0)=$id WITH r0,STARTNODE(r0) AS n, ENDNODE(r0) AS m";
        }
    }

    @Override
    public String build(String label, String property) {
        if (label == null || label.isEmpty()) {
            return "MATCH ()-[r0]->() WHERE r0.`" + property + "`=$id WITH r0, STARTNODE(r0) AS n, ENDNODE(r0) AS m";
        } else {
            return "MATCH ()-[r0:`" + label + "`]->() WHERE r0.`" + property + "`=$id  WITH r0, STARTNODE(r0) AS n, ENDNODE(r0) AS m";
        }
    }
}
