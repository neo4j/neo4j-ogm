/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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
package org.neo4j.ogm.cypher;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
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
    public void testFindAllCollection() throws Exception {
        sortOrder.add("distance");
        assertEquals("MATCH ()-[r]-() WHERE ID(r) IN {ids}  WITH r,startnode(r) AS n, endnode(r) AS m ORDER BY r.distance " +
                "MATCH p1 = (n)-[*0..1]-() WITH r, COLLECT(DISTINCT p1) AS startPaths, m " +
                "MATCH p2 = (m)-[*0..1]-() WITH r, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r) AS rId,startPaths + endPaths  AS paths " +
                "UNWIND paths AS p RETURN DISTINCT p, rId", query.findAll(Arrays.asList(1L, 2L, 3L), 1).setSortOrder(sortOrder).getStatement());
    }

    @Test
    public void testFindByLabel() throws Exception {
        sortOrder.add("distance");
        assertEquals("MATCH ()-[r:`ORBITS`]-()  WITH r,startnode(r) AS n, endnode(r) AS m ORDER BY r.distance MATCH p1 = (n)-[*0..3]-() WITH r, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..3]-() WITH r, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId", query.findByType("ORBITS", 3).setSortOrder(sortOrder).getStatement());
    }

    @Test
    public void testFindByProperty() throws Exception {
        filters.add("distance", 60.2);
        sortOrder.add("aphelion");
        assertEquals("MATCH (n)-[r:`ORBITS`]->(m) WHERE r.`distance` = { `distance` }  WITH r,startnode(r) AS n, endnode(r) AS m ORDER BY r.aphelion MATCH p1 = (n)-[*0..1]-() WITH r, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..1]-() WITH r, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId", query.findByType("ORBITS", filters, 1).setSortOrder(sortOrder).getStatement());
    }

    @Test
    public void testMultipleSortOrders() {
        sortOrder.add(SortOrder.Direction.DESC, "distance", "aphelion");
        assertEquals("MATCH ()-[r:`ORBITS`]-()  WITH r,startnode(r) AS n, endnode(r) AS m ORDER BY r.distance DESC,r.aphelion DESC MATCH p1 = (n)-[*0..3]-() WITH r, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..3]-() WITH r, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId", query.findByType("ORBITS", 3).setSortOrder(sortOrder).getStatement());
    }

    @Test
    public void testDifferentSortDirections() {
        sortOrder.add(SortOrder.Direction.DESC, "type").add("name");
        assertEquals("MATCH ()-[r:`ORBITS`]-()  WITH r,startnode(r) AS n, endnode(r) AS m ORDER BY r.type DESC,r.name MATCH p1 = (n)-[*0..3]-() WITH r, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..3]-() WITH r, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId", query.findByType("ORBITS", 3).setSortOrder(sortOrder).getStatement());
    }

}
