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
package org.neo4j.ogm.session.request.strategy.impl;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.session.request.strategy.impl.NodeQueryStatements;

/**
 * @author Vince Bickers
 */
public class NodeEntityQueryPagingTest {

    private final NodeQueryStatements query = new NodeQueryStatements();

    @Test
    public void testFindAll() {
        check("MATCH p=()-->() WITH p SKIP 2 LIMIT 2 RETURN p", query.findAll().setPagination(new Pagination(1, 2)).getStatement());
    }

    @Test
    public void testFindById() {
        check("MATCH (n) WHERE ID(n) IN { ids } WITH n SKIP 2 LIMIT 2 MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", query.findAll(Arrays.asList(23L, 24L), 1).setPagination(new Pagination(1, 2)).getStatement());
    }

    @Test
    public void testFindByType() {
        check("MATCH (n:`Raptor`) WITH n SKIP 4 LIMIT 2 MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", query.findByType("Raptor", 1).setPagination(new Pagination(2, 2)).getStatement());
    }

    @Test
    public void testFindByProperty() {
        check("MATCH (n:`Raptor`) WHERE n.`name` = { `name_0` } WITH n SKIP 0 LIMIT 2 MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)", query.findByType("Raptor", new Filters().add(new Filter("name", ComparisonOperator.EQUALS, "velociraptor")), 2).setPagination(new Pagination(0, 2)).getStatement());
    }

    @Test
    public void testFindByIdDepthZero() {
        check("MATCH (n) WHERE ID(n) IN { ids } WITH n SKIP 1 LIMIT 1 RETURN n", query.findAll(Arrays.asList(23L, 24L), 0).setPagination(new Pagination(1, 1)).getStatement());
    }

    @Test
    public void testFindByTypeDepthZero() {
        check("MATCH (n:`Raptor`) WITH n SKIP 4 LIMIT 2 RETURN n", query.findByType("Raptor", 0).setPagination(new Pagination(2, 2)).getStatement());
    }

    @Test
    public void testByPropertyDepthZero() {
        check("MATCH (n:`Raptor`) WHERE n.`name` = { `name_0` } WITH n SKIP 0 LIMIT 2 RETURN n", query.findByType("Raptor", new Filters().add(new Filter("name", ComparisonOperator.EQUALS, "velociraptor")), 0).setPagination(new Pagination(0, 2)).getStatement());
    }

    @Test
    public void testFindByIdDepthInfinite() {
        check("MATCH (n) WHERE ID(n) IN { ids } WITH n SKIP 2 LIMIT 2 MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)", query.findAll(Arrays.asList(23L, 24L), -1).setPagination(new Pagination(1, 2)).getStatement());
    }

    @Test
    public void testFindByTypeDepthInfinite() {
        check("MATCH (n:`Raptor`) WITH n SKIP 6 LIMIT 2 MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)", query.findByType("Raptor", -1).setPagination(new Pagination(3, 2)).getStatement());
    }

    @Test
    public void testFindByPropertyDepthInfinite() {
        check("MATCH (n:`Raptor`) WHERE n.`name` = { `name_0` }  WITH n SKIP 0 LIMIT 2 MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)", query.findByType("Raptor", new Filters().add(new Filter("name", ComparisonOperator.EQUALS, "velociraptor")), -1).setPagination(new Pagination(0, 2)).getStatement());
    }

    @Test
    public void testFindByTypeAndOffset() {
        Pagination pagination = new Pagination(1, 5);
        pagination.setOffset(3);
        check("MATCH (n:`Raptor`) WITH n SKIP 3 LIMIT 5 MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", query.findByType("Raptor", 1).setPagination(pagination).getStatement());
    }

    private void check(String expected, String actual) {
        assertEquals(expected, actual);
    }
}
