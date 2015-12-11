/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */
package org.neo4j.ogm.cypher;

import org.junit.Test;
import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.exception.InvalidDepthException;
import org.neo4j.ogm.exception.MissingOperatorException;
import org.neo4j.ogm.session.request.strategy.QueryStatements;
import org.neo4j.ogm.session.request.strategy.VariableDepthRelationshipQuery;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class RelationshipEntityQueryTest {

    private final QueryStatements query = new VariableDepthRelationshipQuery();

    @Test
    public void testFindOne() throws Exception {
        assertEquals("MATCH (n)-[r]->() WHERE ID(r) = { id } WITH n MATCH p=(n)-[*0..2]-(m) RETURN p", query.findOne(0L, 2).getStatement());
    }

    @Test
    public void testFindAllCollection() throws Exception {
        assertEquals("MATCH (n)-[r]->() WHERE ID(r) IN { ids } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p", query.findAll(Arrays.asList(1L, 2L, 3L), 1).getStatement());
    }

    @Test
    public void testFindAll() throws Exception {
        assertEquals("MATCH p=()-->() RETURN p", query.findAll().getStatement());
    }

    @Test
    public void testFindByLabel() throws Exception {
        assertEquals("MATCH p=()-[r:`ORBITS`*..3]-() RETURN p", query.findByType("ORBITS", 3).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-707
     */
    @Test
    public void testFindAllByTypeCollection() throws Exception {
        assertEquals("MATCH (n)-[r:`ORBITS`]->() WHERE ID(r) IN { ids } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p", query.findAllByType("ORBITS", Arrays.asList(1L, 2L, 3L), 1).getStatement());
    }

    @Test
    public void testFindByProperty() throws Exception {
        assertEquals("MATCH (n)-[r:`ORBITS`]->(m) WHERE r.`distance` = { `distance` } WITH n,r MATCH p=(n)-[*0..4]-() RETURN p, ID(r)", query.findByProperties("ORBITS", new Filters().add(new Filter("distance", 60.2)), 4).getStatement());
    }

    @Test(expected = InvalidDepthException.class)
    public void testFindOneZeroDepth() throws Exception {
        query.findOne(0L, 0).getStatement();
    }

    @Test(expected = InvalidDepthException.class)
    public void testFindAllCollectionZeroDepth() throws Exception {
        query.findAll(Arrays.asList(1L, 2L, 3L), 0).getStatement();
    }

    @Test(expected = InvalidDepthException.class)
    public void testFindByLabelZeroDepth() throws Exception {
        query.findByType("ORBITS", 0).getStatement();
    }

    @Test(expected = InvalidDepthException.class)
    public void testFindByPropertyZeroDepth() throws Exception {
        query.findByProperties("ORBITS", new Filters().add(new Filter("perihelion", 19.7)), 0).getStatement();
    }

    @Test(expected = InvalidDepthException.class)
    public void testFindOneInfiniteDepth() throws Exception {
        query.findOne(0L, -1).getStatement();
    }

    @Test(expected = InvalidDepthException.class)
    public void testFindAllCollectionInfiniteDepth() throws Exception {
        query.findAll(Arrays.asList(1L, 2L, 3L), -1).getStatement();
    }

    @Test(expected = InvalidDepthException.class)
    public void testFindByLabelInfiniteDepth() throws Exception {
        query.findByType("ORBITS", -1).getStatement();
    }

    @Test(expected = InvalidDepthException.class)
    public void testFindByPropertyInfiniteDepth() throws Exception {
        query.findByProperties("ORBITS", new Filters().add(new Filter("period", 2103.776)), -1).getStatement();
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByNestedPropertyOutgoing() throws Exception {
        Filter planetFilter = new Filter();
        planetFilter.setNestedPropertyName("world");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setPropertyValue("Earth");
        planetFilter.setPropertyName("name");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection("OUTGOING");
        planetFilter.setComparisonOperator(ComparisonOperator.EQUALS);
        assertEquals("MATCH (n:`Planet`) WHERE n.`name` = { `world_name` } MATCH (n)-[r:`ORBITS`]->(m) WITH n,r MATCH p=(n)-[*0..4]-() RETURN p, ID(r)", query.findByProperties("ORBITS", new Filters().add(planetFilter), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByNestedPropertyIncoming() throws Exception {
        Filter planetFilter = new Filter();
        planetFilter.setNestedPropertyName("world");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setPropertyValue("Earth");
        planetFilter.setPropertyName("name");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection("INCOMING");
        planetFilter.setComparisonOperator(ComparisonOperator.EQUALS);
        assertEquals("MATCH (m:`Planet`) WHERE m.`name` = { `world_name` } MATCH (n)-[r:`ORBITS`]->(m) WITH n,r MATCH p=(n)-[*0..4]-() RETURN p, ID(r)", query.findByProperties("ORBITS", new Filters().add(planetFilter), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByMultipleNestedProperties() throws Exception {
        Filter planetNameFilter = new Filter();
        planetNameFilter.setNestedPropertyName("world");
        planetNameFilter.setNestedEntityTypeLabel("Planet");
        planetNameFilter.setPropertyValue("Earth");
        planetNameFilter.setPropertyName("name");
        planetNameFilter.setRelationshipType("ORBITS");
        planetNameFilter.setRelationshipDirection("OUTGOING");
        planetNameFilter.setComparisonOperator(ComparisonOperator.EQUALS);

        Filter planetMoonsFilter = new Filter();
        planetMoonsFilter.setNestedPropertyName("moons");
        planetMoonsFilter.setNestedEntityTypeLabel("Planet");
        planetMoonsFilter.setPropertyValue("Earth");
        planetMoonsFilter.setPropertyName("moons");
        planetMoonsFilter.setRelationshipType("ORBITS");
        planetMoonsFilter.setRelationshipDirection("OUTGOING");
        planetMoonsFilter.setBooleanOperator(BooleanOperator.AND);
        planetMoonsFilter.setComparisonOperator(ComparisonOperator.EQUALS);

        assertEquals("MATCH (n:`Planet`) WHERE n.`name` = { `world_name` } AND n.`moons` = { `moons_moons` } MATCH (n)-[r:`ORBITS`]->(m) WITH n,r MATCH p=(n)-[*0..4]-() RETURN p, ID(r)", query.findByProperties("ORBITS", new Filters().add(planetNameFilter, planetMoonsFilter), 4).getStatement());
    }


    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByMultipleNestedPropertiesOnBothEnds() throws Exception {
        Filter moonFilter = new Filter();
        moonFilter.setNestedPropertyName("world");
        moonFilter.setNestedEntityTypeLabel("Moon");
        moonFilter.setPropertyValue("Earth");
        moonFilter.setPropertyName("name");
        moonFilter.setRelationshipType("ORBITS");
        moonFilter.setRelationshipDirection("OUTGOING");
        moonFilter.setComparisonOperator(ComparisonOperator.EQUALS);

        Filter planetFilter = new Filter();
        planetFilter.setNestedPropertyName("colour");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setPropertyValue("Red");
        planetFilter.setPropertyName("colour");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection("INCOMING");
        planetFilter.setComparisonOperator(ComparisonOperator.EQUALS);

        assertEquals("MATCH (n:`Moon`) WHERE n.`name` = { `world_name` } MATCH (m:`Planet`) WHERE m.`colour` = { `colour_colour` } MATCH (n)-[r:`ORBITS`]->(m) WITH n,r MATCH p=(n)-[*0..4]-() RETURN p, ID(r)", query.findByProperties("ORBITS", new Filters().add(moonFilter, planetFilter), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByPropertiesAnded() throws Exception {
        Filter distance = new Filter("distance", 60.2);
        Filter time = new Filter("time", 3600);
        time.setBooleanOperator(BooleanOperator.AND);
        assertEquals("MATCH (n)-[r:`ORBITS`]->(m) WHERE r.`distance` = { `distance` } AND r.`time` = { `time` } WITH n,r MATCH p=(n)-[*0..4]-() RETURN p, ID(r)", query.findByProperties("ORBITS", new Filters().add(distance, time), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByPropertiesOred() throws Exception {
        Filter distance = new Filter("distance", 60.2);
        Filter time = new Filter("time", 3600);
        time.setBooleanOperator(BooleanOperator.OR);
        assertEquals("MATCH (n)-[r:`ORBITS`]->(m) WHERE r.`distance` = { `distance` } OR r.`time` = { `time` } WITH n,r MATCH p=(n)-[*0..4]-() RETURN p, ID(r)", query.findByProperties("ORBITS", new Filters().add(distance, time), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByPropertiesWithDifferentComparisonOperatorsAnded() throws Exception {
        Filter distance = new Filter("distance", 60.2);
        distance.setComparisonOperator(ComparisonOperator.LESS_THAN);
        Filter time = new Filter("time", 3600);
        time.setBooleanOperator(BooleanOperator.AND);
        assertEquals("MATCH (n)-[r:`ORBITS`]->(m) WHERE r.`distance` < { `distance` } AND r.`time` = { `time` } WITH n,r MATCH p=(n)-[*0..4]-() RETURN p, ID(r)", query.findByProperties("ORBITS", new Filters().add(distance, time), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByPropertiesWithDifferentComparisonOperatorsOred() throws Exception {
        Filter distance = new Filter("distance", 60.2);
        Filter time = new Filter("time", 3600);
        time.setBooleanOperator(BooleanOperator.OR);
        time.setComparisonOperator(ComparisonOperator.GREATER_THAN);
        assertEquals("MATCH (n)-[r:`ORBITS`]->(m) WHERE r.`distance` = { `distance` } OR r.`time` > { `time` } WITH n,r MATCH p=(n)-[*0..4]-() RETURN p, ID(r)", query.findByProperties("ORBITS", new Filters().add(distance, time), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByBaseAndNestedPropertyOutgoing() throws Exception {
        Filter planetFilter = new Filter();
        planetFilter.setNestedPropertyName("world");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setPropertyValue("Earth");
        planetFilter.setPropertyName("name");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection("OUTGOING");
        planetFilter.setComparisonOperator(ComparisonOperator.EQUALS);
        Filter time = new Filter("time", 3600);
        time.setBooleanOperator(BooleanOperator.AND);
        assertEquals("MATCH (n:`Planet`) WHERE n.`name` = { `world_name` } MATCH (n)-[r:`ORBITS`]->(m) WHERE r.`time` = { `time` } WITH n,r MATCH p=(n)-[*0..4]-() RETURN p, ID(r)", query.findByProperties("ORBITS", new Filters().add(planetFilter, time), 4).getStatement());
    }


    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByBaseAndNestedPropertyIncoming() throws Exception {
        Filter planetFilter = new Filter();
        planetFilter.setNestedPropertyName("world");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setPropertyValue("Earth");
        planetFilter.setPropertyName("name");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection("INCOMING");
        planetFilter.setComparisonOperator(ComparisonOperator.EQUALS);
        Filter time = new Filter("time", 3600);
        assertEquals("MATCH (m:`Planet`) WHERE m.`name` = { `world_name` } MATCH (n)-[r:`ORBITS`]->(m) WHERE r.`time` = { `time` } WITH n,r MATCH p=(n)-[*0..4]-() RETURN p, ID(r)", query.findByProperties("ORBITS", new Filters().add(planetFilter, time), 4).getStatement());
    }


    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByBaseAndMultipleNestedPropertiesOnBothEnds() throws Exception {
        Filter moonFilter = new Filter();
        moonFilter.setNestedPropertyName("world");
        moonFilter.setNestedEntityTypeLabel("Moon");
        moonFilter.setPropertyValue("Earth");
        moonFilter.setPropertyName("name");
        moonFilter.setRelationshipType("ORBITS");
        moonFilter.setRelationshipDirection("OUTGOING");
        moonFilter.setComparisonOperator(ComparisonOperator.EQUALS);

        Filter planetFilter = new Filter();
        planetFilter.setNestedPropertyName("colour");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setPropertyValue("Red");
        planetFilter.setPropertyName("colour");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection("INCOMING");
        planetFilter.setComparisonOperator(ComparisonOperator.EQUALS);


        Filter time = new Filter("time", 3600);
        time.setBooleanOperator(BooleanOperator.AND);

        assertEquals("MATCH (n:`Moon`) WHERE n.`name` = { `world_name` } MATCH (m:`Planet`) WHERE m.`colour` = { `colour_colour` } MATCH (n)-[r:`ORBITS`]->(m) WHERE r.`time` = { `time` } WITH n,r MATCH p=(n)-[*0..4]-() RETURN p, ID(r)", query.findByProperties("ORBITS", new Filters().add(moonFilter, planetFilter, time), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see Issue #73
     */
    @Test(expected = MissingOperatorException.class)
    public void testFindByPropertiesAndedWithMissingBooleanOperator() throws Exception {
        Filter distance = new Filter("distance", 60.2);
        Filter time = new Filter("time", 3600);
        query.findByProperties("ORBITS", new Filters().add(distance, time), 4).getStatement();
    }

    /**
     * @throws Exception
     * @see Issue #73
     */
    @Test(expected = MissingOperatorException.class)
    public void testFindByMultipleNestedPropertiesMissingBooleanOperator() throws Exception {
        Filter planetNameFilter = new Filter();
        planetNameFilter.setNestedPropertyName("world");
        planetNameFilter.setNestedEntityTypeLabel("Planet");
        planetNameFilter.setPropertyValue("Earth");
        planetNameFilter.setPropertyName("name");
        planetNameFilter.setRelationshipType("ORBITS");
        planetNameFilter.setRelationshipDirection("OUTGOING");
        planetNameFilter.setComparisonOperator(ComparisonOperator.EQUALS);

        Filter planetMoonsFilter = new Filter();
        planetMoonsFilter.setNestedPropertyName("moons");
        planetMoonsFilter.setNestedEntityTypeLabel("Planet");
        planetMoonsFilter.setPropertyValue("Earth");
        planetMoonsFilter.setPropertyName("moons");
        planetMoonsFilter.setRelationshipType("ORBITS");
        planetMoonsFilter.setRelationshipDirection("OUTGOING");
        planetMoonsFilter.setComparisonOperator(ComparisonOperator.EQUALS);

        query.findByProperties("ORBITS", new Filters().add(planetNameFilter, planetMoonsFilter), 4).getStatement();
    }
}
