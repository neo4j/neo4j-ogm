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

import org.junit.Test;
import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.AbstractRequest;
import org.neo4j.ogm.exception.MissingOperatorException;
import org.neo4j.ogm.session.request.strategy.QueryStatements;
import org.neo4j.ogm.session.request.strategy.VariableDepthQuery;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class NodeEntityQueryTest {

    private final QueryStatements queryStatements = new VariableDepthQuery();

    @Test
    public void testFindOne() throws Exception {
        assertEquals("MATCH (n) WHERE id(n) = { id } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p", queryStatements.findOne(0L, 2).getStatement());
    }

    @Test
    public void testFindAllCollection() throws Exception {
        assertEquals("MATCH (n) WHERE id(n) in { ids } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p", queryStatements.findAll(Arrays.asList(1L, 2L, 3L), 1).getStatement());
    }

    @Test
    public void testFindAll() throws Exception {
        assertEquals("MATCH p=()-->() RETURN p", queryStatements.findAll().getStatement());
    }

    @Test
    public void testFindByLabel() throws Exception {
        assertEquals("MATCH (n:`Orbit`) WITH n MATCH p=(n)-[*0..3]-(m) RETURN p", queryStatements.findByType("Orbit", 3).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-707
     */
    @Test
    public void testFindAllByLabel() throws Exception {
        assertEquals("MATCH (n:`Orbit`) WHERE id(n) in { ids } RETURN n", queryStatements.findAllByType("Orbit", Arrays.asList(1L, 2L, 3L), 0).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-707
     */
    @Test
    public void testFindAllByLabelDepthOne() throws Exception {
        assertEquals("MATCH (n:`Orbit`) WHERE id(n) in { ids } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p", queryStatements.findAllByType("Orbit", Arrays.asList(1L, 2L, 3L), 1).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-707
     */
    @Test
    public void testFindAllByLabelDepthInfinity() throws Exception {
        assertEquals("MATCH (n:`Orbit`) WHERE id(n) in { ids } WITH n MATCH p=(n)-[*0..]-(m) RETURN p", queryStatements.findAllByType("Orbit", Arrays.asList(1L, 2L, 3L), -1).getStatement());
    }

    @Test
    public void testFindByProperty() throws Exception {
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` = { `diameter` } WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add("diameter", 60.2), 4).getStatement());
    }

    @Test
    public void testFindOneZeroDepth() throws Exception {
        assertEquals("MATCH (n) WHERE id(n) = { id } RETURN n", queryStatements.findOne(0L, 0).getStatement());
    }

    @Test
    public void testFindAllCollectionZeroDepth() throws Exception {
        assertEquals("MATCH (n) WHERE id(n) in { ids } RETURN n", queryStatements.findAll(Arrays.asList(1L, 2L, 3L), 0).getStatement());
    }

    @Test
    public void testFindByLabelZeroDepth() throws Exception {
        assertEquals("MATCH (n:`Orbit`) RETURN n", queryStatements.findByType("Orbit", 0).getStatement());
    }

    @Test
    public void testFindByPropertyZeroDepth() throws Exception {
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` = { `diameter` } RETURN n", queryStatements.findByProperties("Asteroid", new Filters().add("diameter", 60.2), 0).getStatement());
    }


    @Test
    /**
     * @see DATAGRAPH-781
     * @throws Exception
     */
    public void testFindByPropertyWithNegativeValue() throws Exception {
        AbstractRequest abstractRequest = queryStatements.findByProperties("Asteroid", new Filters().add("albedo", -12.2), 0);

        assertEquals("MATCH (n:`Asteroid`) WHERE n.`albedo` = { `albedo` } RETURN n", abstractRequest.getStatement());
        assertEquals(-12.2, (double) abstractRequest.getParameters().get("albedo"), 0.005);
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
     * @throws Exception
     * @see DATAGRAPH-595
     */
    @Test
    public void testFindOneNegativeDepth() throws Exception {
        assertEquals("MATCH (n) WHERE id(n) = { id } WITH n MATCH p=(n)-[*0..]-(m) RETURN p", queryStatements.findOne(0L, -1).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-595
     */
    @Test
    public void testFindAllCollectionNegativeDepth() throws Exception {
        assertEquals("MATCH (n) WHERE id(n) in { ids } WITH n MATCH p=(n)-[*0..]-(m) RETURN p", queryStatements.findAll(Arrays.asList(1L, 2L, 3L), -1).getStatement());
    }


    /**
     * @throws Exception
     * @see DATAGRAPH-595
     */
    @Test
    public void testFindByLabelNegativeDepth() throws Exception {
        assertEquals("MATCH (n:`Orbit`) WITH n MATCH p=(n)-[*0..]-(m) RETURN p", queryStatements.findByType("Orbit", -1).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-595
     */
    @Test
    public void testFindByPropertyNegativeDepth() throws Exception {
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` = { `diameter` }  WITH n MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add("diameter", 60.2), -1).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-631
     */
    @Test
    public void testFindByPropertyWithIllegalCharacters() throws Exception {
        assertEquals("MATCH (n:`Studio`) WHERE n.`studio-name` = { `studio-name` } WITH n MATCH p=(n)-[*0..3]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Studio", new Filters().add("studio-name", "Abbey Road Studios"), 3).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByPropertyGreaterThan() throws Exception {
        Filter parameter = new Filter("diameter", 60);
        parameter.setComparisonOperator(ComparisonOperator.GREATER_THAN);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` > { `diameter` } WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(parameter), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByPropertyLessThan() throws Exception {
        Filter parameter = new Filter("diameter", 60);
        parameter.setComparisonOperator(ComparisonOperator.LESS_THAN);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` < { `diameter` } WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(parameter), 4).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleAndPropertiesLessThan() {
        Filter nameParam = new Filter("name", "AST-1");
        Filter diameterParam = new Filter("diameter", 60);
        diameterParam.setComparisonOperator(ComparisonOperator.LESS_THAN);
        diameterParam.setBooleanOperator(BooleanOperator.AND);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`name` = { `name` } AND n.`diameter` < { `diameter` } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(nameParam).add(diameterParam), 2).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleAndPropertiesGreaterThan() {
        Filter nameParam = new Filter("name", "AST-1");
        Filter diameterParam = new Filter("diameter", 60);
        diameterParam.setComparisonOperator(ComparisonOperator.GREATER_THAN);
        diameterParam.setBooleanOperator(BooleanOperator.AND);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`name` = { `name` } AND n.`diameter` > { `diameter` } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(nameParam).add(diameterParam), 2).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleAndPropertiesGreaterThanWithDifferentOrder() {
        Filter nameParam = new Filter("name", "AST-1");
        nameParam.setBooleanOperator(BooleanOperator.AND);
        Filter diameterParam = new Filter("diameter", 60);
        diameterParam.setComparisonOperator(ComparisonOperator.GREATER_THAN);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` > { `diameter` } AND n.`name` = { `name` } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(diameterParam).add(nameParam), 2).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleOrPropertiesGreaterThan() {
        Filter nameParam = new Filter("name", "AST-1");
        Filter diameterParam = new Filter("diameter", 60);
        diameterParam.setComparisonOperator(ComparisonOperator.GREATER_THAN);
        diameterParam.setBooleanOperator(BooleanOperator.OR);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`name` = { `name` } OR n.`diameter` > { `diameter` } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(nameParam).add(diameterParam), 2).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleOrPropertiesLessThan() {
        Filter nameParam = new Filter("name", "AST-1");
        Filter diameterParam = new Filter("diameter", 60);
        diameterParam.setComparisonOperator(ComparisonOperator.LESS_THAN);
        diameterParam.setBooleanOperator(BooleanOperator.OR);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`name` = { `name` } OR n.`diameter` < { `diameter` } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(nameParam).add(diameterParam), 2).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleOrPropertiesGreaterThanWithDifferentOrder() {
        Filter nameParam = new Filter("name", "AST-1");
        nameParam.setBooleanOperator(BooleanOperator.OR);
        Filter diameterParam = new Filter("diameter", 60);
        diameterParam.setComparisonOperator(ComparisonOperator.GREATER_THAN);
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` > { `diameter` } OR n.`name` = { `name` } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(diameterParam).add(nameParam), 2).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByNestedPropertyOutgoing() {
        Filter planetParam = new Filter();
        planetParam.setPropertyName("name");
        planetParam.setPropertyValue("Earth");
        planetParam.setComparisonOperator(ComparisonOperator.EQUALS);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");
        assertEquals("MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name` } MATCH (n)-[:`COLLIDES`]->(m0) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(planetParam), 1).getStatement());

    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByNestedPropertyIncoming() {
        Filter planetParam = new Filter();
        planetParam.setPropertyName("name");
        planetParam.setPropertyValue("Earth");
        planetParam.setComparisonOperator(ComparisonOperator.EQUALS);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("INCOMING");
        assertEquals("MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name` } MATCH (n)<-[:`COLLIDES`]-(m0) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(planetParam), 1).getStatement());

    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByNestedPropertyUndirected() {
        Filter planetParam = new Filter();
        planetParam.setPropertyName("name");
        planetParam.setPropertyValue("Earth");
        planetParam.setComparisonOperator(ComparisonOperator.EQUALS);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("UNDIRECTED");
        assertEquals("MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name` } MATCH (n)-[:`COLLIDES`]-(m0) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(planetParam), 1).getStatement());

    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleNestedProperties() {
        Filter diameterParam = new Filter("diameter", 60);
        diameterParam.setComparisonOperator(ComparisonOperator.GREATER_THAN);

        Filter planetParam = new Filter();
        planetParam.setPropertyName("name");
        planetParam.setPropertyValue("Earth");
        planetParam.setComparisonOperator(ComparisonOperator.EQUALS);
        planetParam.setBooleanOperator(BooleanOperator.AND);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` > { `diameter` } MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name` } MATCH (n)-[:`COLLIDES`]->(m0) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(diameterParam).add(planetParam), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleNestedPropertiesInfiniteDepth() {
        Filter diameterParam = new Filter("diameter", 60);
        diameterParam.setComparisonOperator(ComparisonOperator.GREATER_THAN);

        Filter planetParam = new Filter();
        planetParam.setPropertyName("name");
        planetParam.setPropertyValue("Earth");
        planetParam.setComparisonOperator(ComparisonOperator.EQUALS);
        planetParam.setBooleanOperator(BooleanOperator.AND);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` > { `diameter` } MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name` } MATCH (n)-[:`COLLIDES`]->(m0)  WITH n MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(diameterParam).add(planetParam), -1).getStatement());
    }

    /**
     * @see DATAGRAPH-662
     * //TODO FIXME
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testFindByMultipleNestedPropertiesOred() {
        Filter diameterParam = new Filter("diameter", 60);
        diameterParam.setComparisonOperator(ComparisonOperator.GREATER_THAN);

        Filter planetParam = new Filter();
        planetParam.setPropertyName("name");
        planetParam.setPropertyValue("Earth");
        planetParam.setComparisonOperator(ComparisonOperator.EQUALS);
        planetParam.setBooleanOperator(BooleanOperator.OR);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");
        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` > { `diameter` } OPTIONAL MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name` } OPTIONAL MATCH (n)-[:`COLLIDES`]->(m0) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(diameterParam).add(planetParam), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-662
     * //TODO FIXME
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testFindByMultipleNestedPropertiesOredDepth0() {
        Filter diameterParam = new Filter("diameter", 60);
        diameterParam.setComparisonOperator(ComparisonOperator.GREATER_THAN);

        Filter planetParam = new Filter();
        planetParam.setPropertyName("name");
        planetParam.setPropertyValue("Earth");
        planetParam.setComparisonOperator(ComparisonOperator.EQUALS);
        planetParam.setBooleanOperator(BooleanOperator.OR);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");

        assertEquals("MATCH (n:`Asteroid`) WHERE n.`diameter` > { `diameter` } OPTIONAL MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name` } OPTIONAL MATCH (n)-[:`COLLIDES`]->(m0) RETURN n", queryStatements.findByProperties("Asteroid", new Filters().add(diameterParam).add(planetParam), 0).getStatement());
    }

    /**
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByNestedREProperty() {
        Filter planetParam = new Filter();
        planetParam.setPropertyName("totalDestructionProbability");
        planetParam.setPropertyValue("20");
        planetParam.setComparisonOperator(ComparisonOperator.EQUALS);
        planetParam.setNestedPropertyName("collision");
        planetParam.setNestedEntityTypeLabel("Collision"); //Collision is an RE
        planetParam.setNestedRelationshipEntity(true);
        planetParam.setRelationshipType("COLLIDES"); //assume COLLIDES is the RE type
        planetParam.setRelationshipDirection("OUTGOING");
        planetParam.setNestedRelationshipEntity(true);

        assertEquals("MATCH (n:`Asteroid`) MATCH (n)-[r:`COLLIDES`]->(m0) WHERE r.`totalDestructionProbability` = { `collision_totalDestructionProbability` } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(planetParam), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByNestedBaseAndREProperty() {
        Filter planetParam = new Filter();
        planetParam.setPropertyName("totalDestructionProbability");
        planetParam.setPropertyValue("20");
        planetParam.setComparisonOperator(ComparisonOperator.EQUALS);
        planetParam.setNestedPropertyName("collision");
        planetParam.setNestedEntityTypeLabel("Collision"); //Collision is an RE
        planetParam.setNestedRelationshipEntity(true);
        planetParam.setRelationshipType("COLLIDES"); //assume COLLIDES is the RE type
        planetParam.setRelationshipDirection("OUTGOING");
        planetParam.setNestedRelationshipEntity(true);

        Filter moonParam = new Filter();
        moonParam.setPropertyName("name");
        moonParam.setPropertyValue("Moon");
        moonParam.setComparisonOperator(ComparisonOperator.EQUALS);
        moonParam.setNestedPropertyName("moon");
        moonParam.setNestedEntityTypeLabel("Moon");
        moonParam.setRelationshipType("ORBITS");
        moonParam.setRelationshipDirection("INCOMING");
        moonParam.setBooleanOperator(BooleanOperator.AND);

        assertEquals("MATCH (n:`Asteroid`) MATCH (n)-[r:`COLLIDES`]->(m0) WHERE r.`totalDestructionProbability` = { `collision_totalDestructionProbability` } MATCH (m1:`Moon`) WHERE m1.`name` = { `moon_name` } MATCH (n)<-[:`ORBITS`]-(m1) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(planetParam, moonParam), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByDifferentNestedPropertiesAnded() {
        Filter planetParam = new Filter();
        planetParam.setPropertyName("name");
        planetParam.setPropertyValue("Earth");
        planetParam.setComparisonOperator(ComparisonOperator.EQUALS);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");

        Filter moonParam = new Filter();
        moonParam.setPropertyName("name");
        moonParam.setPropertyValue("Moon");
        moonParam.setComparisonOperator(ComparisonOperator.EQUALS);
        moonParam.setNestedPropertyName("moon");
        moonParam.setNestedEntityTypeLabel("Moon");
        moonParam.setRelationshipType("ORBITS");
        moonParam.setRelationshipDirection("INCOMING");
        moonParam.setBooleanOperator(BooleanOperator.AND);
        assertEquals("MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name` } MATCH (m1:`Moon`) WHERE m1.`name` = { `moon_name` } MATCH (n)-[:`COLLIDES`]->(m0) MATCH (n)<-[:`ORBITS`]-(m1) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(planetParam).add(moonParam), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-662
     * //TODO FIXME
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testFindByDifferentNestedPropertiesOred() {
        Filter planetParam = new Filter();
        planetParam.setPropertyName("name");
        planetParam.setPropertyValue("Earth");
        planetParam.setComparisonOperator(ComparisonOperator.EQUALS);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");

        Filter moonParam = new Filter();
        moonParam.setPropertyName("name");
        moonParam.setPropertyValue("Moon");
        moonParam.setComparisonOperator(ComparisonOperator.EQUALS);
        moonParam.setNestedPropertyName("moon");
        moonParam.setNestedEntityTypeLabel("Moon");
        moonParam.setRelationshipType("ORBITS");
        moonParam.setRelationshipDirection("INCOMING");
        moonParam.setBooleanOperator(BooleanOperator.OR);
        assertEquals("MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name` } OPTIONAL MATCH (m1:`Moon`) WHERE m1.`name` = { `moon_name` } OPTIONAL MATCH (n)-[:`COLLIDES`]->(m0) OPTIONAL MATCH (n)<-[:`ORBITS`]-(m1) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(planetParam).add(moonParam), 1).getStatement());
    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void testFindByMultipleNestedPropertiesAnded() {
        Filter planetParam = new Filter();
        planetParam.setPropertyName("name");
        planetParam.setPropertyValue("Earth");
        planetParam.setComparisonOperator(ComparisonOperator.EQUALS);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection("OUTGOING");

        Filter moonParam = new Filter();
        moonParam.setPropertyName("size");
        moonParam.setPropertyValue("5");
        moonParam.setComparisonOperator(ComparisonOperator.EQUALS);
        moonParam.setNestedPropertyName("collidesWith");
        moonParam.setNestedEntityTypeLabel("Planet");
        moonParam.setRelationshipType("COLLIDES");
        moonParam.setRelationshipDirection("OUTGOING");
        moonParam.setBooleanOperator(BooleanOperator.AND);
        assertEquals("MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = { `collidesWith_name` } AND m0.`size` = { `collidesWith_size` } MATCH (n)-[:`COLLIDES`]->(m0) WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)", queryStatements.findByProperties("Asteroid", new Filters().add(planetParam).add(moonParam), 1).getStatement());
    }

    /**
     * @see Issue 73
     */
    @Test(expected = MissingOperatorException.class)
    public void testFindByMultipleAndPropertiesWithMissingBooleanOperator() {
        Filter nameParam = new Filter("name", "AST-1");
        Filter diameterParam = new Filter("diameter", 60);
        diameterParam.setComparisonOperator(ComparisonOperator.LESS_THAN);
        queryStatements.findByProperties("Asteroid", new Filters().add(nameParam).add(diameterParam), 2).getStatement();
    }

}
