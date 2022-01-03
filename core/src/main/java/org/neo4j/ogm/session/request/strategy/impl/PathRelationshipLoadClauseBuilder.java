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
package org.neo4j.ogm.session.request.strategy.impl;

import org.neo4j.ogm.session.request.strategy.LoadClauseBuilder;

/**
 * Path based load clause builder for nodes - starts from given relationship variable
 *
 * @author Frantisek Hartman
 */
public class PathRelationshipLoadClauseBuilder implements LoadClauseBuilder {

    @Override
    public String build(String label, int depth) {
        return build("r", label, depth);
    }

    @Override
    public String build(String variable, String label, int depth) {
        return " MATCH p1 = (n)-[*0.." + depth + "]-() " +
            "WITH " + variable + ", COLLECT(DISTINCT p1) AS startPaths, m " +
            "MATCH p2 = (m)-[*0.." + depth + "]-() " +
            "WITH " + variable + ", startPaths, COLLECT(DISTINCT p2) AS endPaths " +
            "WITH " + variable + ",startPaths + endPaths  AS paths " +
            "UNWIND paths AS p " +
            "RETURN DISTINCT p";
    }
}
