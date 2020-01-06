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
package org.neo4j.ogm.session.request.strategy.impl;

import org.neo4j.ogm.session.request.strategy.MatchClauseBuilder;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class IdMatchClauseBuilder implements MatchClauseBuilder {

    @Override
    public String build(String label) {
        if (label == null || label.isEmpty()) {
            return "MATCH (n) WHERE ID(n) = $id WITH n";
        } else {
            return "MATCH (n:`" + label + "`) WHERE ID(n) = $id WITH n";
        }
    }

    @Override
    public String build(String label, String property) {
        if (label == null || label.isEmpty()) {
            return "MATCH (n) WHERE n.`" + property + "` = $id WITH n";
        } else {
            return "MATCH (n:`" + label + "`) WHERE n.`" + property + "` = $id WITH n";
        }
    }
}
