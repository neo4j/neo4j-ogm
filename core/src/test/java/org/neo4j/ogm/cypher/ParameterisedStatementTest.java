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
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.neo4j.ogm.json.ObjectMapperFactory;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.session.request.strategy.impl.NodeDeleteStatements;
import org.neo4j.ogm.session.request.strategy.impl.RelationshipDeleteStatements;
import org.neo4j.ogm.session.request.strategy.impl.NodeQueryStatements;
import org.neo4j.ogm.session.request.strategy.impl.RelationshipQueryStatements;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class ParameterisedStatementTest {

    private static final ObjectMapper mapper = ObjectMapperFactory.objectMapper();
    private Statement statement;

    @Test
    public void testFindOne() throws Exception {
        statement = new NodeQueryStatements().findOne(123L, 1);
        assertEquals("MATCH (n) WHERE ID(n) = { id } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p", statement.getStatement());
        assertEquals("{\"id\":123}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void testFindAllWithIds() throws Exception {
        List<Long> ids = Arrays.asList(new Long[]{123L, 234L, 345L});
        statement = new NodeQueryStatements().findAll(ids, 1);
        assertEquals("MATCH (n) WHERE ID(n) IN { ids } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p", statement.getStatement());
        assertEquals("{\"ids\":[123,234,345]}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void testFindByLabel() throws Exception {
        statement = new NodeQueryStatements().findByType("NODE", 1);
        assertEquals("MATCH (n:`NODE`) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    /**
     * @see DATAGRAPH-589
     */
    @Test
    public void testFindByTypeWithIllegalCharacter() throws Exception {
        statement = new RelationshipQueryStatements().findByType("HAS-ALBUM", 1);
        assertEquals("MATCH ()-[r:`HAS-ALBUM`]-()  WITH r,startnode(r) AS n, endnode(r) AS m MATCH p1 = (n)-[*0..1]-() WITH r, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..1]-() WITH r, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findAll() throws Exception {
        statement = new NodeQueryStatements().findAll();
        assertEquals("MATCH p=()-->() RETURN p", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyStringValue() throws Exception {
        statement = new NodeQueryStatements().findByType("Asteroid", new Filters().add(new Filter("ref", "45 Eugenia")), 1);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`ref` = { `ref_0` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", statement.getStatement());
        assertEquals("{\"ref_0\":\"45 Eugenia\"}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyWildcardLike() throws JsonProcessingException {
        Filter filter = new Filter("ref", "*nia");
        filter.setComparisonOperator(ComparisonOperator.LIKE);
        statement = new NodeQueryStatements().findByType("Asteroid", new Filters().add(filter), 1);
        assertEquals("{\"ref_0\":\"(?i).*nia\"}", mapper.writeValueAsString(statement.getParameters()));
    }


    @Test
    public void findByPropertyIntegralValue() throws Exception {
        statement = new NodeQueryStatements().findByType("Asteroid", new Filters().add(new Filter("index", 77)), 1);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`index` = { `index_0` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", statement.getStatement());
        assertEquals("{\"index_0\":77}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyStandardForm() throws Exception {
        statement = new NodeQueryStatements().findByType("Asteroid", new Filters().add(new Filter("diameter", 6.02E1)), 1);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` = { `diameter_0` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", statement.getStatement());
        assertEquals("{\"diameter_0\":60.2}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyDecimal() throws Exception {
        statement = new NodeQueryStatements().findByType("Asteroid", new Filters().add(new Filter("diameter", 60.2)), 1);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` = { `diameter_0` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", statement.getStatement());
        assertEquals("{\"diameter_0\":60.2}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyEmbeddedDelimiter() throws Exception {
        statement = new NodeQueryStatements().findByType("Cookbooks", new Filters().add(new Filter("title", "Mrs Beeton's Household Recipes")), 1);
        assertEquals("MATCH (n:`Cookbooks`) WHERE n.`title` = { `title_0` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", statement.getStatement());
        assertEquals("{\"title_0\":\"Mrs Beeton's Household Recipes\"}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void delete() throws Exception {
        statement = new NodeDeleteStatements().delete(123L);
        assertEquals("MATCH (n) WHERE ID(n) = { id } OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{\"id\":123}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void deleteAll() throws Exception {
        List<Long> ids = Arrays.asList(new Long[]{123L, 234L, 345L});
        statement = new NodeDeleteStatements().delete(ids);
        assertEquals("MATCH (n) WHERE ID(n) in { ids } OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{\"ids\":[123,234,345]}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void deleteAllByLabel() throws Exception {
        statement = new NodeDeleteStatements().delete("NODE");
        assertEquals("MATCH (n:`NODE`) OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void purge() throws Exception {
        statement = new NodeDeleteStatements().deleteAll();
        assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    /**
     * @see DATAGRAPH-586
     */
    @Test
    public void deleteRel() throws Exception {
        statement = new RelationshipDeleteStatements().delete(123L);
        assertEquals("MATCH (n)-[r]->() WHERE ID(r) = { id } DELETE r", statement.getStatement());
        assertEquals("{\"id\":123}", mapper.writeValueAsString(statement.getParameters()));
    }

    /**
     * @see DATAGRAPH-586
     */
    @Test
    public void deleteAllRels() throws Exception {
        List<Long> ids = Arrays.asList(new Long[]{123L, 234L, 345L});
        statement = new RelationshipDeleteStatements().delete(ids);
        assertEquals("MATCH (n)-[r]->() WHERE ID(r) IN { ids } DELETE r", statement.getStatement());
        assertEquals("{\"ids\":[123,234,345]}", mapper.writeValueAsString(statement.getParameters()));
    }

    /**
     * @see DATAGRAPH-586
     */
    @Test
    public void deleteAllRelsByType() throws Exception {
        statement = new RelationshipDeleteStatements().delete("REL");
        assertEquals("MATCH (n)-[r:`REL`]-() DELETE r", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-631
     */
    @Test
    public void testFindByPropertyWithIllegalCharacter() throws Exception {
        statement = new RelationshipQueryStatements().findByType("HAS-ALBUM", new Filters().add(new Filter("fake-property", "none")), 1);
        assertEquals("MATCH (n)-[r:`HAS-ALBUM`]->(m) WHERE r.`fake-property` = { `fake-property` }  WITH r,startnode(r) AS n, endnode(r) AS m MATCH p1 = (n)-[*0..1]-() WITH r, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..1]-() WITH r, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId", statement.getStatement());
        assertEquals("{\"fake-property\":\"none\"}", mapper.writeValueAsString(statement.getParameters()));

    }

}
