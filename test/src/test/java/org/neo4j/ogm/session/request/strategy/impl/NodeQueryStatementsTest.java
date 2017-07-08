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

import org.junit.Test;
import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.function.DistanceComparison;
import org.neo4j.ogm.cypher.function.DistanceFromPoint;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.exception.MissingOperatorException;
import org.neo4j.ogm.session.request.strategy.QueryStatements;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Jasper Blues
 */
public class NodeQueryStatementsTest {

    /**
     * QueryStatements with graph id
     */
    private final QueryStatements<Long> queryStatements = new NodeQueryStatements<>();

    /**
     * QueryStatements with primary index property
     */
    private final QueryStatements<String> primaryQueryStatements = new NodeQueryStatements<>("uuid", new PathLoadClauseBuilder());

    @Test
    public void testFindOne() throws Exception {
        assertEquals("MATCH (n) WHERE ID(n) = { id } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p", queryStatements.findOne(0L, 2).getStatement());
    }

    @Test
    public void testFindOnePrimaryIndex() throws Exception {
        PagingAndSortingQuery query = primaryQueryStatements.findOne("test-uuid", 2);
        assertEquals("MATCH (n) WHERE n.`uuid` = { id } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p", query.getStatement());
        assertThat(query.getParameters()).containsEntry("id", "test-uuid");
    }

    @Test
    public void testFindOneByType() throws Exception {
        PagingAndSortingQuery query = queryStatements.findOneByType("Orbit", 0L, 2);
        assertThat(query.getStatement())
            .isEqualTo("MATCH (n:`Orbit`) WHERE ID(n) = { id } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p");
        assertThat(query.getParameters()).containsEntry("id", 0L);

        // Also assert that an empty label is the same as using the typeless variant
        assertEquals(queryStatements.findOneByType("", 0L, 2).getStatement(), queryStatements.findOne(0L, 2).getStatement());
        assertEquals(queryStatements.findOneByType(null, 0L, 2).getStatement(), queryStatements.findOne(0L, 2).getStatement());
    }

    @Test
    public void testFindOneByTypePrimaryIndex() throws Exception {
        PagingAndSortingQuery query = primaryQueryStatements.findOneByType("Orbit", "test-uuid", 2);

        assertThat(query.getStatement())
                .isEqualTo("MATCH (n:`Orbit`) WHERE n.`uuid` = { id } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p");
        assertThat(query.getParameters()).containsEntry("id", "test-uuid");
    }

    @Test
    public void testFindOneByTypePrimaryIndexInfiniteDepth() throws Exception {
        PagingAndSortingQuery query = primaryQueryStatements.findOneByType("Orbit", "test-uuid", -1);

        assertThat(query.getStatement())
                .isEqualTo("MATCH (n:`Orbit`) WHERE n.`uuid` = { id } WITH n MATCH p=(n)-[*0..]-(m) RETURN p");
        assertThat(query.getParameters()).containsEntry("id", "test-uuid");
    }

    @Test
    public void testFindByLabel() throws Exception {
        String statement = queryStatements.findByType("Orbit", 3).getStatement();
        assertEquals("MATCH (n:`Orbit`) WITH n MATCH p=(n)-[*0..3]-(m) RETURN p", statement);
    }

    @Test
    public void testFindByDistance() {
        DistanceComparison function = new DistanceComparison(new DistanceFromPoint(37.4d, 112.9d, 1000.0d));
        Filters filters = new Filters().add(new Filter(function, ComparisonOperator.EQUALS));
        String statement = queryStatements.findByType("Restaurant",
                filters, 4).getStatement();
        assertEquals("MATCH (n:`Restaurant`) WHERE distance(point(n),point({latitude:{lat}, longitude:{lon}})) = {distance} WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)", statement);
    }

    @Test
    public void testFindByPropertyIsNull() {

        Filter isNull = new Filter("score", ComparisonOperator.IS_NULL, null);

        String statement = queryStatements.findByType("Restaurant", new Filters().add(isNull), 3).getStatement();
        assertEquals("MATCH (n:`Restaurant`) WHERE n.`score` IS NULL WITH n MATCH p=(n)-[*0..3]-(m) RETURN p, ID(n)", statement);

        Filter isNotNull = new Filter("score", ComparisonOperator.IS_NULL, null);
        isNotNull.setNegated(true);

        statement = queryStatements.findByType("Restaurant", new Filters().add(isNotNull), 3).getStatement();
        assertEquals("MATCH (n:`Restaurant`) WHERE NOT(n.`score` IS NULL ) WITH n MATCH p=(n)-[*0..3]-(m) RETURN p, ID(n)", statement);
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-707
     */
    @Test
    public void testFindAllByLabel() throws Exception {
        assertEquals("MATCH (n:`Orbit`) WHERE ID(n) IN { ids } WITH n RETURN n", queryStatements.findAllByType("Orbit", Arrays.asList(1L, 2L, 3L), 0).getStatement());
    }

    @Test
    public void testFindAllByLabelPrimaryIndex() throws Exception {
        List<String> ids = Arrays.asList("uuid-1", "uuid-2");
        PagingAndSortingQuery query = primaryQueryStatements.findAllByType("Orbit", ids, 0);

        assertThat(query.getStatement())
                .isEqualTo("MATCH (n:`Orbit`) WHERE n.`uuid` IN { ids } WITH n RETURN n");
        assertThat(query.getParameters())
                .containsEntry("ids", ids);
    }

    @Test
    public void testFindAllByLabelPrimaryIndexInfiniteDepth() throws Exception {
        List<String> ids = Arrays.asList("uuid-1", "uuid-2");
        PagingAndSortingQuery query = primaryQueryStatements.findAllByType("Orbit", ids, -1);

        assertThat(query.getStatement())
                .isEqualTo("MATCH (n:`Orbit`) WHERE n.`uuid` IN { ids } WITH n MATCH p=(n)-[*0..]-(m) RETURN p");
        assertThat(query.getParameters())
                .containsEntry("ids", ids);
    }

    /**
     * @see DATAGRAPH-707
     */
    @Test
    public void testFindAllByLabelDepthOne() throws Exception {
        assertEquals("MATCH (n:`Orbit`) WHERE ID(n) IN { ids } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p", queryStatements.findAllByType("Orbit", Arrays.asList(1L, 2L, 3L), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-707
     */
    @Test
    public void testFindAllByLabelDepthInfinity() throws Exception {
        assertEquals("MATCH (n:`Orbit`) WHERE ID(n) IN { ids } WITH n MATCH p=(n)-[*0..]-(m) RETURN p", queryStatements.findAllByType("Orbit", Arrays.asList(1L, 2L, 3L), -1).getStatement());
    }

    @Test
    public void testFindByProperty() throws Exception {
        String statement = queryStatements.findByType("Asteroid",
                new Filters().add(new Filter("diameter", ComparisonOperator.EQUALS, 60.2)), 4).getStatement();
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` = { `diameter_0` } WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)", statement);
    }

    @Test
    public void testFindOneZeroDepth() throws Exception {
        assertEquals("MATCH (n) WHERE ID(n) = { id } WITH n RETURN n", queryStatements.findOne(0L, 0).getStatement());
    }

    @Test
    public void testFindOneZeroDepthPrimaryIndes() throws Exception {
        PagingAndSortingQuery query = primaryQueryStatements.findOne("test-uuid", 0);

        assertThat(query.getStatement()).isEqualTo("MATCH (n) WHERE n.`uuid` = { id } WITH n RETURN n");
    }

    @Test
    public void testFindByLabelZeroDepth() throws Exception {
        assertEquals("MATCH (n:`Orbit`) WITH n RETURN n", queryStatements.findByType("Orbit", 0).getStatement());
    }

    @Test
    public void testFindByPropertyZeroDepth() throws Exception {
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` = { `diameter_0` } WITH n RETURN n", queryStatements.findByType("Asteroid", new Filters().add(new Filter("diameter", ComparisonOperator.EQUALS, 60.2)), 0).getStatement());
    }


    @Test
    /**
     * @see DATAGRAPH-781
     * @throws Exception
     */
    public void testFindByPropertyWithInfiniteValue() throws Exception {
        PagingAndSortingQuery pagingAndSortingQuery = queryStatements.findByType("Asteroid", new Filters().add(new Filter("albedo", ComparisonOperator.EQUALS, -12.2)), 0);

        assertEquals("MATCH (n:`Asteroid`) WHERE n.`albedo` = { `albedo_0` } WITH n RETURN n", pagingAndSortingQuery.getStatement());
        assertEquals(-12.2, (double) pagingAndSortingQuery.getParameters().get("albedo_0"), 0.005);
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-589
     */
    @Test
    public void testFindByLabelWithIllegalCharacters() throws Exception {
        assertEquals("MATCH (n:`l'artiste`) WITH n MATCH p=(n)-[*0..3]-(m) RETURN p", queryStatements.findByType("l'artiste", 3).getStatement());
    }

    /**
     * @see DATAGRAPH-595
     */
    @Test
    public void testFindOneInfiniteDepth() throws Exception {
        assertEquals("MATCH (n) WHERE ID(n) = { id } WITH n MATCH p=(n)-[*0..]-(m) RETURN p", queryStatements.findOne(0L, -1).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-595
     */
    @Test
    public void testFindByLabelInfiniteDepth() throws Exception {
        assertEquals("MATCH (n:`Orbit`) WITH n MATCH p=(n)-[*0..]-(m) RETURN p", queryStatements.findByType("Orbit", -1).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-595
     */
    @Test
    public void testFindByPropertyInfiniteDepth() throws Exception {
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` = { `diameter_0` } WITH n MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)", queryStatements.findByType("Asteroid", new Filters().add(new Filter("diameter", ComparisonOperator.EQUALS, 60.2)), -1).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-631
     */
    @Test
    public void testFindByPropertyWithIllegalCharacters() throws Exception {
        assertEquals("MATCH (n:`Studio`) WHERE n.`studio-name` = { `studio-name_0` } WITH n MATCH p=(n)-[*0..3]-(m) RETURN p, ID(n)", queryStatements.findByType("Studio", new Filters().add(new Filter("studio-name", ComparisonOperator.EQUALS, "Abbey Road Studios")), 3).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByPropertyGreaterThan() throws Exception {
        Filter parameter = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` > { `diameter_0` } WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)",
                queryStatements.findByType("Asteroid", new Filters().add(parameter), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-904
     */
    @Test
    public void testFindByPropertyGreaterThanEqual() throws Exception {
        Filter parameter = new Filter("diameter", ComparisonOperator.GREATER_THAN_EQUAL, 60);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` >= { `diameter_0` } WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)",
                queryStatements.findByType("Asteroid", new Filters().add(parameter), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByPropertyLessThan() throws Exception {
        Filter parameter = new Filter("diameter", ComparisonOperator.LESS_THAN, 60);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` < { `diameter_0` } " +
                        "WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)",
                queryStatements.findByType("Asteroid",
                        new Filters().add(parameter), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-904
     */
    @Test
    public void testFindByPropertyLessThanEqual() throws Exception {
        Filter parameter = new Filter("diameter", ComparisonOperator.LESS_THAN_EQUAL, 60);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` <= { `diameter_0` } " +
                        "WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)",
                queryStatements.findByType("Asteroid",
                        new Filters().add(parameter), 4).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleAndPropertiesLessThan() {
        Filter nameParam = new Filter("name", ComparisonOperator.EQUALS, "AST-1");
        Filter diameterParam = new Filter("diameter", ComparisonOperator.LESS_THAN, 60);
        diameterParam.setBooleanOperator(BooleanOperator.AND);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`name` = { `name_0` } AND n.`diameter` < { `diameter_1` } " +
                        "WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)",
                queryStatements.findByType("Asteroid",
                        new Filters().add(nameParam).add(diameterParam), 2).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleAndPropertiesGreaterThan() {
        Filter nameParam = new Filter("name", ComparisonOperator.EQUALS, "AST-1");
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);
        diameterParam.setBooleanOperator(BooleanOperator.AND);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`name` = { `name_0` } AND n.`diameter` > { `diameter_1` } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)", queryStatements.findByType("Asteroid", new Filters().add(nameParam).add(diameterParam), 2).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleAndPropertiesGreaterThanWithDifferentOrder() {
        Filter nameParam = new Filter("name", ComparisonOperator.EQUALS, "AST-1");
        nameParam.setBooleanOperator(BooleanOperator.AND);
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` > { `diameter_0` } AND n.`name` = { `name_1` } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)", queryStatements.findByType("Asteroid", new Filters().add(diameterParam).add(nameParam), 2).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleOrPropertiesGreaterThan() {
        Filter nameParam = new Filter("name", ComparisonOperator.EQUALS, "AST-1");
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);
        diameterParam.setBooleanOperator(BooleanOperator.OR);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`name` = { `name_0` } OR n.`diameter` > { `diameter_1` } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)", queryStatements.findByType("Asteroid", new Filters().add(nameParam).add(diameterParam), 2).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleOrPropertiesLessThan() {
        Filter nameParam = new Filter("name", ComparisonOperator.EQUALS, "AST-1");
        Filter diameterParam = new Filter("diameter", ComparisonOperator.LESS_THAN, 60);
        diameterParam.setBooleanOperator(BooleanOperator.OR);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`name` = { `name_0` } OR n.`diameter` < { `diameter_1` } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)", queryStatements.findByType("Asteroid", new Filters().add(nameParam).add(diameterParam), 2).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleOrPropertiesGreaterThanWithDifferentOrder() {
        Filter nameParam = new Filter("name", ComparisonOperator.EQUALS, "AST-1");
        nameParam.setBooleanOperator(BooleanOperator.OR);
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` > { `diameter_0` } OR n.`name` = { `name_1` } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)", queryStatements.findByType("Asteroid", new Filters().add(diameterParam).add(nameParam), 2).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByNestedPropertyOutgoing() {
        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");
        assertEquals("MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name_0` } MATCH (n)-[:`COLLIDES`]->(m0) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)",
                queryStatements.findByType("Asteroid", new Filters().add(planetParam), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-904
     */
    @Test
    public void testFindByNestedPropertySameEntityType() {
        Filter filter = new Filter("name", ComparisonOperator.EQUALS, "Vesta");
        filter.setNestedPropertyName("collidesWith");
        filter.setNestedEntityTypeLabel("Asteroid");
        filter.setRelationshipType("COLLIDES");
        filter.setRelationshipDirection("OUTGOING");
        assertEquals("MATCH (n:`Asteroid`) MATCH (m0:`Asteroid`) WHERE m0.`name` = { `collidesWith_name_0` } MATCH (n)-[:`COLLIDES`]->(m0) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)",
                queryStatements.findByType("Asteroid", new Filters().add(filter), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByNestedPropertyIncoming() {
        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("INCOMING");
        assertEquals("MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name_0` } MATCH (n)<-[:`COLLIDES`]-(m0) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByType("Asteroid", new Filters().add(planetParam), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByNestedPropertyUndirected() {
        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("UNDIRECTED");
        assertEquals("MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name_0` } MATCH (n)-[:`COLLIDES`]-(m0) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByType("Asteroid", new Filters().add(planetParam), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleNestedProperties() {
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);

        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setBooleanOperator(BooleanOperator.AND);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` > { `diameter_0` } MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name_1` } MATCH (n)-[:`COLLIDES`]->(m0) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByType("Asteroid", new Filters().add(diameterParam).add(planetParam), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleNestedPropertiesInfiniteDepth() {
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);

        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setBooleanOperator(BooleanOperator.AND);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` > { `diameter_0` } MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name_1` } MATCH (n)-[:`COLLIDES`]->(m0) WITH n MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)", queryStatements.findByType("Asteroid", new Filters().add(diameterParam).add(planetParam), -1).getStatement());
    }

    /**
     * @see DATAGRAPH-662
     * //TODO FIXME
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testFindByMultipleNestedPropertiesOred() {
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);

        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setBooleanOperator(BooleanOperator.OR);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` > { `diameter` } OPTIONAL MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name` } OPTIONAL MATCH (n)-[:`COLLIDES`]->(m0) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByType("Asteroid", new Filters().add(diameterParam).add(planetParam), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-662
     * //TODO FIXME
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testFindByMultipleNestedPropertiesOredDepth0() {
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);

        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setBooleanOperator(BooleanOperator.OR);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");

        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` > { `diameter` } OPTIONAL MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name` } OPTIONAL MATCH (n)-[:`COLLIDES`]->(m0) RETURN n", queryStatements.findByType("Asteroid", new Filters().add(diameterParam).add(planetParam), 0).getStatement());
    }

    /**
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByNestedREProperty() {
        Filter planetParam = new Filter("totalDestructionProbability", ComparisonOperator.EQUALS, "20");
        planetParam.setNestedPropertyName("collision");
        planetParam.setNestedEntityTypeLabel("Collision"); //Collision is an RE
        planetParam.setNestedRelationshipEntity(true);
        planetParam.setRelationshipType("COLLIDES"); //assume COLLIDES is the RE type
        planetParam.setRelationshipDirection("OUTGOING");
        planetParam.setNestedRelationshipEntity(true);

        assertEquals("MATCH (n:`Asteroid`) MATCH (n)-[r0:`COLLIDES`]->(m0) " +
                        "WHERE r0.`totalDestructionProbability` = { `collision_totalDestructionProbability_0` } " +
                        "WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)",
                queryStatements.findByType("Asteroid", new Filters().add(planetParam), 1).getStatement());
    }

    /**
     * @see OGM-279
     */
    @Test
    public void testFindByMultipleNestedREProperty() {
        Filter planetParam = new Filter("totalDestructionProbability", ComparisonOperator.EQUALS, "20");
        planetParam.setNestedPropertyName("collision");
        planetParam.setNestedEntityTypeLabel("Collision"); //Collision is an RE
        planetParam.setNestedRelationshipEntity(true);
        planetParam.setRelationshipType("COLLIDES"); //assume COLLIDES is the RE type
        planetParam.setRelationshipDirection("OUTGOING");
        planetParam.setNestedRelationshipEntity(true);

        Filter satelliteParam = new Filter("signalStrength", ComparisonOperator.GREATER_THAN_EQUAL, "400");
        satelliteParam.setBooleanOperator(BooleanOperator.AND);
        satelliteParam.setNestedPropertyName("monitoringSatellites");
        satelliteParam.setNestedEntityTypeLabel("Satellite"); //Collision is an RE
        satelliteParam.setNestedRelationshipEntity(true);
        satelliteParam.setRelationshipType("MONITORED_BY"); //assume COLLIDES is the RE type
        satelliteParam.setRelationshipDirection("INCOMING");
        satelliteParam.setNestedRelationshipEntity(true);

        assertEquals("MATCH (n:`Asteroid`) MATCH (n)-[r0:`COLLIDES`]->(m0) WHERE r0.`totalDestructionProbability` = { `collision_totalDestructionProbability_0` } " +
                        "MATCH (n)<-[r1:`MONITORED_BY`]-(m1) WHERE r1.`signalStrength` >= { `monitoringSatellites_signalStrength_1` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)",
                queryStatements.findByType("Asteroid", new Filters().add(planetParam).add(satelliteParam), 1).getStatement());
    }


    /**
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByNestedBaseAndREProperty() {
        Filter planetParam = new Filter("totalDestructionProbability", ComparisonOperator.EQUALS, "20");
        planetParam.setNestedPropertyName("collision");
        planetParam.setNestedEntityTypeLabel("Collision"); //Collision is an RE
        planetParam.setNestedRelationshipEntity(true);
        planetParam.setRelationshipType("COLLIDES"); //assume COLLIDES is the RE type
        planetParam.setRelationshipDirection("OUTGOING");
        planetParam.setNestedRelationshipEntity(true);

        Filter moonParam = new Filter("name", ComparisonOperator.EQUALS, "Moon");
        moonParam.setNestedPropertyName("moon");
        moonParam.setNestedEntityTypeLabel("Moon");
        moonParam.setRelationshipType("ORBITS");
        moonParam.setRelationshipDirection("INCOMING");
        moonParam.setBooleanOperator(BooleanOperator.AND);

        assertEquals("MATCH (n:`Asteroid`) MATCH (n)-[r0:`COLLIDES`]->(m0) " +
                        "WHERE r0.`totalDestructionProbability` = { `collision_totalDestructionProbability_0` } " +
                        "MATCH (m1:`Moon`) WHERE m1.`name` = { `moon_name_1` } MATCH (n)<-[:`ORBITS`]-(m1) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)",
                queryStatements.findByType("Asteroid", new Filters().add(planetParam, moonParam), 1).getStatement());
    }


    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByDifferentNestedPropertiesAnded() {
        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");

        Filter moonParam = new Filter("name", ComparisonOperator.EQUALS, "Moon");
        moonParam.setNestedPropertyName("moon");
        moonParam.setNestedEntityTypeLabel("Moon");
        moonParam.setRelationshipType("ORBITS");
        moonParam.setRelationshipDirection("INCOMING");
        moonParam.setBooleanOperator(BooleanOperator.AND);
        assertEquals("MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name_0` } " +
                        "MATCH (m1:`Moon`) WHERE m1.`name` = { `moon_name_1` } MATCH (n)-[:`COLLIDES`]->(m0) " +
                        "MATCH (n)<-[:`ORBITS`]-(m1) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)",
                queryStatements.findByType("Asteroid",
                        new Filters().add(planetParam).add(moonParam), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-662
     * //TODO FIXME
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testFindByDifferentNestedPropertiesOred() {
        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");

        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");

        Filter moonParam = new Filter("name", ComparisonOperator.EQUALS, "Moon");
        moonParam.setNestedPropertyName("moon");
        moonParam.setNestedEntityTypeLabel("Moon");
        moonParam.setRelationshipType("ORBITS");
        moonParam.setRelationshipDirection("INCOMING");
        moonParam.setBooleanOperator(BooleanOperator.OR);
        assertEquals("MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name` } OPTIONAL MATCH (m1:`Moon`) WHERE m1.`name` = { `moon_name` } OPTIONAL MATCH (n)-[:`COLLIDES`]->(m0) OPTIONAL MATCH (n)<-[:`ORBITS`]-(m1) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByType("Asteroid", new Filters().add(planetParam).add(moonParam), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleNestedPropertiesAnded() {
        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");

        Filter moonParam = new Filter("size", ComparisonOperator.EQUALS, "5");
        moonParam.setNestedPropertyName("collidesWith");
        moonParam.setNestedEntityTypeLabel("Planet");
        moonParam.setRelationshipType("COLLIDES");
        moonParam.setRelationshipDirection("OUTGOING");
        moonParam.setBooleanOperator(BooleanOperator.AND);
        assertEquals("MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name_0` } AND m0.`size` = { `collidesWith_size_1` } MATCH (n)-[:`COLLIDES`]->(m0) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByType("Asteroid", new Filters().add(planetParam).add(moonParam), 1).getStatement());
    }

    /**
     * @see Issue 73
     */
    @Test(expected = MissingOperatorException.class)
    public void testFindByMultipleAndPropertiesWithMissingBooleanOperator() {
        Filter nameParam = new Filter("name", ComparisonOperator.EQUALS, "AST-1");
        Filter diameterParam = new Filter("diameter", ComparisonOperator.LESS_THAN, 60);
        queryStatements.findByType("Asteroid", new Filters().add(nameParam).add(diameterParam), 2).getStatement();
    }
}
