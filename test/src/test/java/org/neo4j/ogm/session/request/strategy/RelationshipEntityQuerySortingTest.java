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
package org.neo4j.ogm.session.request.strategy;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.session.request.strategy.QueryStatements;
import org.neo4j.ogm.session.request.strategy.impl.RelationshipQueryStatements;

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
    public void testFindByLabel() throws Exception {
        sortOrder.add("distance");
        String statement = query.findByType("ORBITS", 3).setSortOrder(sortOrder).getStatement();
        String expected = "MATCH ()-[r0:`ORBITS`]-()  WITH r0,startnode(r0) AS n, endnode(r0) AS m " +
                "ORDER BY r0.distance MATCH p1 = (n)-[*0..3]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
                "MATCH p2 = (m)-[*0..3]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId";
        assertEquals(expected, statement);
    }

    @Test
    public void testFindByProperty() throws Exception {
        filters.add(new Filter("distance", ComparisonOperator.EQUALS, 60.2));
        sortOrder.add("aphelion");
        String expected = "MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` = { `distance_0` }  " +
                "WITH r0,startnode(r0) AS n, endnode(r0) AS m ORDER BY r0.aphelion " +
                "MATCH p1 = (n)-[*0..1]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
                "MATCH p2 = (m)-[*0..1]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                "WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId";
        assertEquals(expected, query.findByType("ORBITS", filters, 1).setSortOrder(sortOrder).getStatement());
    }

    @Test
    public void testMultipleSortOrders() {
        sortOrder.add(SortOrder.Direction.DESC, "distance", "aphelion");
        String statement = query.findByType("ORBITS", 3).setSortOrder(sortOrder).getStatement();
        String expected = "MATCH ()-[r0:`ORBITS`]-()  WITH r0,startnode(r0) AS n, " +
                "endnode(r0) AS m ORDER BY r0.distance DESC,r0.aphelion DESC " +
                "MATCH p1 = (n)-[*0..3]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
                "MATCH p2 = (m)-[*0..3]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                "WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId";
        assertEquals(expected, statement);
    }

    @Test
    public void testDifferentSortDirections() {
        sortOrder.add(SortOrder.Direction.DESC, "type").add("name");
        String statement = query.findByType("ORBITS", 3).setSortOrder(sortOrder).getStatement();
        String expected = "MATCH ()-[r0:`ORBITS`]-()  WITH r0,startnode(r0) AS n, endnode(r0) AS m " +
                "ORDER BY r0.type DESC,r0.name MATCH p1 = (n)-[*0..3]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
                "MATCH p2 = (m)-[*0..3]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r0) AS rId,startPaths + endPaths  AS paths " +
                "UNWIND paths AS p RETURN DISTINCT p, rId";
        assertEquals(expected, statement);
    }
}
