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

import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.session.request.strategy.QueryStatements;
import org.neo4j.ogm.session.request.strategy.impl.RelationshipQueryStatements;

/**
 * @author Vince Bickers
 */
public class RelationshipEntityQueryPagingTest {

    private final QueryStatements query = new RelationshipQueryStatements();

    @Test
    public void testFindAllCollection() throws Exception {
        assertEquals("MATCH ()-[r0]-() WHERE ID(r0) IN {ids}  WITH r0,startnode(r0) AS n, endnode(r0) AS m SKIP 30 LIMIT 10 " +
                "MATCH p1 = (n)-[*0..1]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
                "MATCH p2 = (m)-[*0..1]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r0) AS rId,startPaths + endPaths  AS paths " +
                "UNWIND paths AS p RETURN DISTINCT p, rId", query.findAll(Arrays.asList(1L, 2L, 3L), 1).setPagination(new Pagination(3, 10)).getStatement());
    }

    @Test
    public void testFindAll() throws Exception {
        assertEquals("MATCH p=()-->() WITH p SKIP 2 LIMIT 2 RETURN p", query.findAll().setPagination(new Pagination(1, 2)).getStatement());
    }

    @Test
    public void testFindByLabel() throws Exception {
        assertEquals("MATCH ()-[r0:`ORBITS`]-()  WITH r0,startnode(r0) AS n, endnode(r0) AS m SKIP 10 LIMIT 10 MATCH p1 = (n)-[*0..3]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..3]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId", query.findByType("ORBITS", 3).setPagination(new Pagination(1, 10)).getStatement());
    }

    @Test
    public void testFindByProperty() throws Exception {
        assertEquals("MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` = { `distance_0` }  WITH r0,startnode(r0) AS n, endnode(r0) AS m SKIP 0 LIMIT 4 MATCH p1 = (n)-[*0..1]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..1]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId", query.findByType("ORBITS", new Filters().add(new Filter("distance", ComparisonOperator.EQUALS, 60.2)), 1).setPagination(new Pagination(0, 4)).getStatement());
    }
}
