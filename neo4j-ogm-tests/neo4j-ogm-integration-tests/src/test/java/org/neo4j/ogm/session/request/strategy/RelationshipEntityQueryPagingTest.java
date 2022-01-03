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
package org.neo4j.ogm.session.request.strategy;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.session.request.strategy.impl.RelationshipQueryStatements;

/**
 * @author Vince Bickers
 */
public class RelationshipEntityQueryPagingTest {

    private final QueryStatements query = new RelationshipQueryStatements();

    @Test
    public void testFindByLabel() throws Exception {
        assertThat(query.findByType("ORBITS", 3).setPagination(new Pagination(1, 10)).getStatement())
            .isEqualTo(
                "MATCH ()-[r0:`ORBITS`]-()  WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m SKIP 10 LIMIT 10 MATCH p1 = (n)-[*0..3]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..3]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)");
    }

    @Test
    public void testFindByProperty() throws Exception {
        assertThat(
            query.findByType("ORBITS", new Filters().add(new Filter("distance", ComparisonOperator.EQUALS, 60.2)), 1)
                .setPagination(new Pagination(0, 4)).getStatement())
            .isEqualTo(
                "MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` = $`distance_0`  WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m SKIP 0 LIMIT 4 MATCH p1 = (n)-[*0..1]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..1]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)");
    }
}
