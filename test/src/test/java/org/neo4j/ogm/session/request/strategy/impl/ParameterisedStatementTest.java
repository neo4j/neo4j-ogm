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

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.neo4j.ogm.config.ObjectMapperFactory;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Jasper Blues
 */
public class ParameterisedStatementTest {

    private static final ObjectMapper mapper = ObjectMapperFactory.objectMapper();
    private NodeQueryStatements nodeQueryStatements = new NodeQueryStatements();
    private PagingAndSortingQuery query;
    private CypherQuery cypherQuery;

    @Test
    public void testFindOne() throws Exception {
        query = nodeQueryStatements.findOne(123L, 1);
        assertThat(query.getStatement()).isEqualTo("MATCH (n) WHERE ID(n) = { id } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p");
        assertThat(mapper.writeValueAsString(query.getParameters())).isEqualTo("{\"id\":123}");
    }

    @Test
    public void testFindByLabel() throws Exception {
        query = nodeQueryStatements.findByType("NODE", 1);
        assertThat(query.getStatement()).isEqualTo("MATCH (n:`NODE`) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p");
        assertThat(mapper.writeValueAsString(query.getParameters())).isEqualTo("{}");
    }

    /**
     * @see DATAGRAPH-589
     */
    @Test
    public void testFindByTypeWithIllegalCharacter() throws Exception {
        query = new RelationshipQueryStatements().findByType("HAS-ALBUM", 1);
        assertThat(query.getStatement()).isEqualTo("MATCH ()-[r0:`HAS-ALBUM`]-()  WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..1]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..1]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId");
        assertThat(mapper.writeValueAsString(query.getParameters())).isEqualTo("{}");
    }

    @Test
    public void findByPropertyStringValue() throws Exception {
        query = nodeQueryStatements.findByType("Asteroid", new Filters().add(new Filter("ref", ComparisonOperator.EQUALS, "45 Eugenia")), 1);
        assertThat(query.getStatement()).isEqualTo("MATCH (n:`Asteroid`) WHERE n.`ref` = { `ref_0` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
        assertThat(mapper.writeValueAsString(query.getParameters())).isEqualTo("{\"ref_0\":\"45 Eugenia\"}");
    }

    @Test
    public void findByPropertyWildcardLike() throws JsonProcessingException {
        Filter filter = new Filter("ref", ComparisonOperator.LIKE, "*nia");
        query = nodeQueryStatements.findByType("Asteroid", new Filters().add(filter), 1);
        assertThat(mapper.writeValueAsString(query.getParameters())).isEqualTo("{\"ref_0\":\"(?i).*nia\"}");
    }


    @Test
    public void findByPropertyIntegralValue() throws Exception {
        query = nodeQueryStatements.findByType("Asteroid", new Filters().add(new Filter("index", ComparisonOperator.EQUALS, 77)), 1);
        assertThat(query.getStatement()).isEqualTo("MATCH (n:`Asteroid`) WHERE n.`index` = { `index_0` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
        assertThat(mapper.writeValueAsString(query.getParameters())).isEqualTo("{\"index_0\":77}");
    }

    @Test
    public void findByPropertyStandardForm() throws Exception {
        query = nodeQueryStatements.findByType("Asteroid", new Filters().add(new Filter("diameter", ComparisonOperator.EQUALS, 6.02E1)), 1);
        assertThat(query.getStatement()).isEqualTo("MATCH (n:`Asteroid`) WHERE n.`diameter` = { `diameter_0` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
        assertThat(mapper.writeValueAsString(query.getParameters())).isEqualTo("{\"diameter_0\":60.2}");
    }

    @Test
    public void findByPropertyDecimal() throws Exception {
        query = nodeQueryStatements.findByType("Asteroid", new Filters().add(new Filter("diameter", ComparisonOperator.EQUALS, 60.2)), 1);
        assertThat(query.getStatement()).isEqualTo("MATCH (n:`Asteroid`) WHERE n.`diameter` = { `diameter_0` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
        assertThat(mapper.writeValueAsString(query.getParameters())).isEqualTo("{\"diameter_0\":60.2}");
    }

    @Test
    public void findByPropertyEmbeddedDelimiter() throws Exception {
        query = nodeQueryStatements.findByType("Cookbooks", new Filters().add(new Filter("title", ComparisonOperator.EQUALS, "Mrs Beeton's Household Recipes")), 1);
        assertThat(query.getStatement()).isEqualTo("MATCH (n:`Cookbooks`) WHERE n.`title` = { `title_0` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
        assertThat(mapper.writeValueAsString(query.getParameters())).isEqualTo("{\"title_0\":\"Mrs Beeton's Household Recipes\"}");
    }

    @Test
    public void delete() throws Exception {
        cypherQuery = new NodeDeleteStatements().delete(123L);
        assertThat(cypherQuery.getStatement()).isEqualTo("MATCH (n) WHERE ID(n) = { id } OPTIONAL MATCH (n)-[r0]-() DELETE r0, n");
        assertThat(mapper.writeValueAsString(cypherQuery.getParameters())).isEqualTo("{\"id\":123}");
    }

    @Test
    public void deleteAll() throws Exception {
        List<Long> ids = Arrays.asList(new Long[]{123L, 234L, 345L});
        cypherQuery = new NodeDeleteStatements().delete(ids);
        assertThat(cypherQuery.getStatement()).isEqualTo("MATCH (n) WHERE ID(n) in { ids } OPTIONAL MATCH (n)-[r0]-() DELETE r0, n");
        assertThat(mapper.writeValueAsString(cypherQuery.getParameters())).isEqualTo("{\"ids\":[123,234,345]}");
    }

    @Test
    public void deleteAllByLabel() throws Exception {
        cypherQuery = new NodeDeleteStatements().delete("NODE");
        assertThat(cypherQuery.getStatement()).isEqualTo("MATCH (n:`NODE`) OPTIONAL MATCH (n)-[r0]-() DELETE r0, n");
        assertThat(mapper.writeValueAsString(cypherQuery.getParameters())).isEqualTo("{}");
    }

    @Test
    public void purge() throws Exception {
        cypherQuery = new NodeDeleteStatements().deleteAll();
        assertThat(cypherQuery.getStatement()).isEqualTo("MATCH (n) OPTIONAL MATCH (n)-[r0]-() DELETE r0, n");
        assertThat(mapper.writeValueAsString(cypherQuery.getParameters())).isEqualTo("{}");
    }

    /**
     * @see DATAGRAPH-586
     */
    @Test
    public void deleteRel() throws Exception {
        cypherQuery = new RelationshipDeleteStatements().delete(123L);
        assertThat(cypherQuery.getStatement()).isEqualTo("MATCH (n)-[r0]->() WHERE ID(r0) = { id } DELETE r0");
        assertThat(mapper.writeValueAsString(cypherQuery.getParameters())).isEqualTo("{\"id\":123}");
    }

    /**
     * @see DATAGRAPH-586
     */
    @Test
    public void deleteAllRels() throws Exception {
        List<Long> ids = Arrays.asList(new Long[]{123L, 234L, 345L});
        cypherQuery = new RelationshipDeleteStatements().delete(ids);
        assertThat(cypherQuery.getStatement()).isEqualTo("MATCH (n)-[r0]->() WHERE ID(r0) IN { ids } DELETE r0");
        assertThat(mapper.writeValueAsString(cypherQuery.getParameters())).isEqualTo("{\"ids\":[123,234,345]}");
    }

    /**
     * @see DATAGRAPH-586
     */
    @Test
    public void deleteAllRelsByType() throws Exception {
        cypherQuery = new RelationshipDeleteStatements().delete("REL");
        assertThat(cypherQuery.getStatement()).isEqualTo("MATCH (n)-[r0:`REL`]-() DELETE r0");
        assertThat(mapper.writeValueAsString(cypherQuery.getParameters())).isEqualTo("{}");
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-631
     */
    @Test
    public void testFindByPropertyWithIllegalCharacter() throws Exception {
        query = new RelationshipQueryStatements().findByType("HAS-ALBUM", new Filters().add(new Filter("fake-property", ComparisonOperator.EQUALS, "none")), 1);
        assertThat(query.getStatement()).isEqualTo("MATCH (n)-[r0:`HAS-ALBUM`]->(m) WHERE r0.`fake-property` = { `fake-property_0` }  WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..1]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..1]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId");
        assertThat(mapper.writeValueAsString(query.getParameters())).isEqualTo("{\"fake-property_0\":\"none\"}");
    }
}
