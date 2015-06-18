/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.unit.mapper.cypher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.session.request.strategy.DeleteNodeStatements;
import org.neo4j.ogm.session.request.strategy.DeleteRelationshipStatements;
import org.neo4j.ogm.session.request.strategy.VariableDepthQuery;
import org.neo4j.ogm.session.request.strategy.VariableDepthRelationshipQuery;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class ParameterisedStatementTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private ParameterisedStatement statement;

    @Test
    public void testFindOne() throws Exception {
        statement = new VariableDepthQuery().findOne(123L, 1);
        assertEquals("MATCH (n) WHERE id(n) = { id } WITH n MATCH p=(n)-[*0..1]-(m) RETURN collect(distinct p)", statement.getStatement());
        assertEquals("{\"id\":123}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void testFindAllWithIds() throws Exception {
        List<Long> ids = Arrays.asList(new Long[] { 123L, 234L, 345L });
        statement = new VariableDepthQuery().findAll(ids, 1);
        assertEquals("MATCH (n) WHERE id(n) in { ids } WITH n MATCH p=(n)-[*0..1]-(m) RETURN collect(distinct p)", statement.getStatement());
        assertEquals("{\"ids\":[123,234,345]}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void testFindByLabel() throws Exception {
        statement = new VariableDepthQuery().findByType("NODE", 1);
        assertEquals("MATCH (n:`NODE`) WITH n MATCH p=(n)-[*0..1]-(m) RETURN collect(distinct p)", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    /**
     * @see DATAGRAPH-589
     */
    @Test
    public void testFindByTypeWithIllegalCharacter() throws Exception {
        statement = new VariableDepthRelationshipQuery().findByType("HAS-ALBUM", 1);
        assertEquals("MATCH p=()-[r:`HAS-ALBUM`*..1]-() RETURN collect(distinct p)", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findAll() throws Exception {
        statement = new VariableDepthQuery().findAll();
        assertEquals("MATCH p=()-->() RETURN p", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyStringValue() throws Exception {
        statement = new VariableDepthQuery().findByProperties("Asteroid", new Filters().add(new Filter("ref", "45 Eugenia")), 1);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`ref` = { `ref` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN collect(distinct p), ID(n)", statement.getStatement());
        assertEquals("{\"ref\":\"45 Eugenia\"}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyIntegralValue() throws Exception {
        statement =  new VariableDepthQuery().findByProperties("Asteroid", new Filters().add(new Filter("index", 77)), 1);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`index` = { `index` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN collect(distinct p), ID(n)",statement.getStatement());
        assertEquals("{\"index\":77}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyStandardForm() throws Exception {
        statement = new VariableDepthQuery().findByProperties("Asteroid", new Filters().add(new Filter("diameter", 6.02E1)), 1);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` = { `diameter` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN collect(distinct p), ID(n)", statement.getStatement());
        assertEquals("{\"diameter\":60.2}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyDecimal() throws Exception {
        statement = new VariableDepthQuery().findByProperties("Asteroid", new Filters().add(new Filter("diameter", 60.2)), 1);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` = { `diameter` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN collect(distinct p), ID(n)", statement.getStatement());
        assertEquals("{\"diameter\":60.2}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyEmbeddedDelimiter() throws Exception {
        statement = new VariableDepthQuery().findByProperties("Cookbooks", new Filters().add(new Filter("title", "Mrs Beeton's Household Recipes")), 1);
        assertEquals("MATCH (n:`Cookbooks`) WHERE n.`title` = { `title` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN collect(distinct p), ID(n)", statement.getStatement());
        assertEquals("{\"title\":\"Mrs Beeton's Household Recipes\"}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void delete() throws Exception {
        statement = new DeleteNodeStatements().delete(123L);
        assertEquals("MATCH (n) WHERE id(n) = { id } OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{\"id\":123}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void deleteAll() throws Exception {
        List<Long> ids = Arrays.asList(new Long[] { 123L, 234L, 345L });
        statement = new DeleteNodeStatements().deleteAll(ids);
        assertEquals("MATCH (n) WHERE id(n) in { ids } OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{\"ids\":[123,234,345]}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void deleteAllByLabel() throws Exception {
        statement = new DeleteNodeStatements().deleteByType("NODE");
        assertEquals("MATCH (n:`NODE`) OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void purge() throws Exception {
        statement = new DeleteNodeStatements().purge();
        assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    /**
     * @see DATAGRAPH-586
     */
    @Test
    public void deleteRel() throws Exception {
        statement = new DeleteRelationshipStatements().delete(123L);
        assertEquals("MATCH (n)-[r]->() WHERE ID(r) = { id } DELETE r", statement.getStatement());
        assertEquals("{\"id\":123}", mapper.writeValueAsString(statement.getParameters()));
    }

    /**
     * @see DATAGRAPH-586
     */
    @Test
    public void deleteAllRels() throws Exception {
        List<Long> ids = Arrays.asList(new Long[]{123L, 234L, 345L});
        statement = new DeleteRelationshipStatements().deleteAll(ids);
        assertEquals("MATCH (n)-[r]->() WHERE id(r) in { ids } DELETE r", statement.getStatement());
        assertEquals("{\"ids\":[123,234,345]}", mapper.writeValueAsString(statement.getParameters()));
    }

    /**
     * @see DATAGRAPH-586
     */
    @Test
    public void deleteAllRelsByType() throws Exception {
        statement = new DeleteRelationshipStatements().deleteByType("REL");
        assertEquals("MATCH (n)-[r:`REL`]-() DELETE r", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    /**
     * @see DATAGRAPH-631
     * @throws Exception
     */
    @Test
    public void testFindByPropertyWithIllegalCharacter() throws Exception {
        statement = new VariableDepthRelationshipQuery().findByProperties("HAS-ALBUM", new Filters().add(new Filter("fake-property", "none")), 1);
        assertEquals("MATCH (n)-[r:`HAS-ALBUM`]->(m) WHERE r.`fake-property` = { `fake-property` } WITH r,n MATCH p=(n)-[*0..1]-() RETURN collect(distinct p), ID(r)", statement.getStatement());
        assertEquals("{\"fake-property\":\"none\"}", mapper.writeValueAsString(statement.getParameters()));

    }

}
