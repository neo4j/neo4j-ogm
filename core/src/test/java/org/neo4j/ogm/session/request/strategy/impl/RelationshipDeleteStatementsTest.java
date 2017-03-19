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
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;

/**
 * @author Vince Bickers
 * @author Jasper Blues
 */
public class RelationshipDeleteStatementsTest {

    private final DeleteStatements statements = new RelationshipDeleteStatements();

    @Test
    public void testDeleteOne() throws Exception {
        assertEquals("MATCH (n)-[r0]->() WHERE ID(r0) = { id } DELETE r0", statements.delete(0L).getStatement());
    }

    @Test
    public void testDeleteMany() throws Exception {
        assertEquals("MATCH (n)-[r0]->() WHERE ID(r0) IN { ids } DELETE r0", statements.delete(Arrays.asList(1L, 2L)).getStatement());
    }

    @Test
    public void testDeleteAll() throws Exception {
        assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r0]-() DELETE r0", statements.deleteAll().getStatement());
    }

    @Test
    public void testDeleteAllAndCount() throws Exception {
        assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r0]-() DELETE r0 RETURN COUNT(r0)", statements.deleteAllAndCount().getStatement());
    }

    @Test
    public void testDeleteAllAndList() throws Exception {
        assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r0]-() DELETE r0 RETURN ID(r0)", statements.deleteAllAndList().getStatement());
    }

    @Test
    public void testDeleteWithType() throws Exception {
        assertEquals("MATCH (n)-[r0:`TRAFFIC_WARDEN`]-() DELETE r0", statements.delete("TRAFFIC_WARDEN").getStatement());
    }

    @Test
    public void testDeleteWithTypeAndCount() throws Exception {
        assertEquals("MATCH (n)-[r0:`TRAFFIC_WARDEN`]-() DELETE r0 RETURN COUNT(r0)", statements.deleteAndCount("TRAFFIC_WARDEN").getStatement());
    }

    @Test
    public void testDeleteWithTypeAndList() throws Exception {
        assertEquals("MATCH (n)-[r0:`TRAFFIC_WARDEN`]-() DELETE r0 RETURN ID(r0)", statements.deleteAndList("TRAFFIC_WARDEN").getStatement());
    }

    @Test
    public void testDeleteWithTypeAndFilters() throws Exception {
        CypherQuery query = statements.delete("INFLUENCE", new Filters().add(new Filter("score", ComparisonOperator.EQUALS, -12.2)));
        assertEquals("MATCH (n)-[r0:`INFLUENCE`]->(m) WHERE r0.`score` = { `score_0` }  DELETE r0", query.getStatement());
    }

    @Test
    public void testDeleteWithTypeAndFiltersAndCount() throws Exception {
        CypherQuery query = statements.deleteAndCount("INFLUENCE", new Filters().add(new Filter("score", ComparisonOperator.EQUALS, -12.2)));
        assertEquals("MATCH (n)-[r0:`INFLUENCE`]->(m) WHERE r0.`score` = { `score_0` }  DELETE r0 RETURN COUNT(r0)", query.getStatement());
    }

    @Test
    public void testDeleteWithTypeAndFiltersAndList() throws Exception {
        CypherQuery query = statements.deleteAndList("INFLUENCE", new Filters().add(new Filter("score", ComparisonOperator.EQUALS, -12.2)));
        assertEquals("MATCH (n)-[r0:`INFLUENCE`]->(m) WHERE r0.`score` = { `score_0` }  DELETE r0 RETURN ID(r0)", query.getStatement());
    }
}
