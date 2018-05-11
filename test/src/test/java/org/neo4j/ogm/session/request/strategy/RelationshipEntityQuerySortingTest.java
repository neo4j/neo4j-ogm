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
package org.neo4j.ogm.session.request.strategy;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.session.request.strategy.impl.RelationshipQueryStatements;

import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.ogm.cypher.ComparisonOperator.EQUALS;
import static org.neo4j.ogm.cypher.query.SortOrder.Direction.DESC;

/**
 * @author Vince Bickers
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
        String expected = "MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` = { `distance_0` }  " +
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
        check(cypher, query.findByType("ORBITS", 3).setSortOrder(sortOrder.add(DESC, "distance", "aphelion")).getStatement());
        check(cypher, query.findByType("ORBITS", 3).setSortOrder(new SortOrder(DESC, "distance", "aphelion")).getStatement());
        check(cypher, query.findByType("ORBITS", 3).setSortOrder(new SortOrder().desc("distance", "aphelion")).getStatement());
    }

    @Test
    public void testDefaultMultipleSortOrders() {
        String cypher = "MATCH ()-[r0:`ORBITS`]-()  WITH DISTINCT(r0) as r0,startnode(r0) AS n, " +
            "endnode(r0) AS m ORDER BY r0.distance,r0.aphelion " +
            "MATCH p1 = (n)-[*0..3]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
            "MATCH p2 = (m)-[*0..3]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
            "WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)";
        check(cypher, query.findByType("ORBITS", 3).setSortOrder(sortOrder.add("distance", "aphelion")).getStatement());
        check(cypher, query.findByType("ORBITS", 3).setSortOrder(new SortOrder("distance", "aphelion")).getStatement());
        check(cypher, query.findByType("ORBITS", 3).setSortOrder(new SortOrder().asc("distance", "aphelion")).getStatement());
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
