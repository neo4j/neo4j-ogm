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
import static org.neo4j.ogm.cypher.ComparisonOperator.*;
import static org.neo4j.ogm.cypher.query.SortOrder.Direction.*;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.session.request.strategy.impl.RelationshipQueryStatements;

/**
 * @author Vince Bickers
 * @author Jonathan D'Orleans
 */
public class RelationshipEntityQuerySortingTest {

    private final QueryStatements query = new RelationshipQueryStatements();
    private SortOrder sortOrder;
    private Filters filters;

    @Before
    public void setUp() {
        sortOrder = new SortOrder();
        filters = new Filters();
    }

    @Test
    public void testFindByLabel() {
        sortOrder.add("distance");
        String statement = query.findByType("ORBITS", 3).setSortOrder(sortOrder).getStatement();
        String expected = "MATCH ()-[r0:`ORBITS`]-()  WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m " +
            "ORDER BY r0.distance MATCH p1 = (n)-[*0..3]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
            "MATCH p2 = (m)-[*0..3]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)";
        assertThat(statement).isEqualTo(expected);
    }

    @Test
    public void testFindByProperty() {
        filters.add(new Filter("distance", EQUALS, 60.2));
        sortOrder.add("aphelion");
        String expected = "MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` = $`distance_0`  " +
            "WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m ORDER BY r0.aphelion " +
            "MATCH p1 = (n)-[*0..1]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
            "MATCH p2 = (m)-[*0..1]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
            "WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)";
        assertThat(query.findByType("ORBITS", filters, 1).setSortOrder(sortOrder).getStatement()).isEqualTo(expected);
    }

    @Test
    public void testMultipleSortOrders() {
        String cypher = "MATCH ()-[r0:`ORBITS`]-()  WITH DISTINCT(r0) as r0,startnode(r0) AS n, " +
            "endnode(r0) AS m ORDER BY r0.distance DESC,r0.aphelion DESC " +
            "MATCH p1 = (n)-[*0..3]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
            "MATCH p2 = (m)-[*0..3]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
            "WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)";

        PagingAndSortingQuery orbitsQuery = query.findByType("ORBITS", 3);
        check(cypher, orbitsQuery.setSortOrder(sortOrder.add(DESC, "distance", "aphelion")).getStatement());
        check(cypher, orbitsQuery.setSortOrder(new SortOrder(DESC, "distance", "aphelion")).getStatement());
        check(cypher, orbitsQuery.setSortOrder(new SortOrder().desc("distance", "aphelion")).getStatement());
    }

    @Test
    public void testDefaultMultipleSortOrders() {
        String cypher = "MATCH ()-[r0:`ORBITS`]-()  WITH DISTINCT(r0) as r0,startnode(r0) AS n, " +
            "endnode(r0) AS m ORDER BY r0.distance,r0.aphelion " +
            "MATCH p1 = (n)-[*0..3]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
            "MATCH p2 = (m)-[*0..3]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
            "WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)";

        PagingAndSortingQuery orbitsQuery = query.findByType("ORBITS", 3);
        check(cypher, orbitsQuery.setSortOrder(sortOrder.add("distance", "aphelion")).getStatement());
        check(cypher, orbitsQuery.setSortOrder(new SortOrder("distance", "aphelion")).getStatement());
        check(cypher, orbitsQuery.setSortOrder(new SortOrder().asc("distance", "aphelion")).getStatement());
    }

    @Test
    public void testDifferentSortDirections() {
        sortOrder.add(DESC, "type").add("name");
        String statement = query.findByType("ORBITS", 3).setSortOrder(sortOrder).getStatement();
        String expected = "MATCH ()-[r0:`ORBITS`]-()  WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m " +
            "ORDER BY r0.type DESC,r0.name MATCH p1 = (n)-[*0..3]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
            "MATCH p2 = (m)-[*0..3]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH r0,startPaths + endPaths  AS paths "
            +
            "UNWIND paths AS p RETURN DISTINCT p, ID(r0)";
        assertThat(statement).isEqualTo(expected);
    }

    private void check(String expected, String actual) {
        assertThat(actual).isEqualTo(expected);
    }
}
