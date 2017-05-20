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
import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.exception.InvalidDepthException;
import org.neo4j.ogm.exception.MissingOperatorException;
import org.neo4j.ogm.session.request.strategy.QueryStatements;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Jasper Blues
 */
public class RelationshipQueryStatementsTest {

    private final QueryStatements query = new RelationshipQueryStatements();

    @Test
    public void testFindOne() throws Exception {
        assertEquals("MATCH ()-[r0]-() WHERE ID(r0)={id}  " +
                        "WITH STARTNODE(r0) AS n, ENDNODE(r0) AS m MATCH p1 = (n)-[*0..2]-() " +
                        "WITH COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..2]-() " +
                        "WITH startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH startPaths + endPaths AS paths UNWIND paths AS p RETURN DISTINCT p",
                query.findOne(0L, 2).getStatement());
    }

    @Test
    public void testFindOneByType() throws Exception {
        assertEquals("MATCH ()-[r0:`ORBITS`]-() WHERE ID(r0)={id}  " +
                        "WITH STARTNODE(r0) AS n, ENDNODE(r0) AS m MATCH p1 = (n)-[*0..2]-() " +
                        "WITH COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..2]-() " +
                        "WITH startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH startPaths + endPaths AS paths UNWIND paths AS p RETURN DISTINCT p",
                query.findOneByType("ORBITS", 0L, 2).getStatement());
    }

    @Test
    public void testFindAllCollection() throws Exception {
        assertEquals("MATCH ()-[r0]-() WHERE ID(r0) IN {ids}  " +
                        "WITH r0,startnode(r0) AS n, endnode(r0) AS m " +
                        "MATCH p1 = (n)-[*0..1]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
                        "MATCH p2 = (m)-[*0..1]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId",
                query.findAll(Arrays.asList(1L, 2L, 3L), 1).getStatement());
    }

    @Test
    public void testFindAll() throws Exception {
        assertEquals("MATCH p=()-->() RETURN p", query.findAll().getStatement());
    }

    @Test
    public void testFindByLabel() throws Exception {
        assertEquals("MATCH ()-[r0:`ORBITS`]-()  WITH r0,startnode(r0) AS n, endnode(r0) AS m " +
                "MATCH p1 = (n)-[*0..3]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
                "MATCH p2 = (m)-[*0..3]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                "WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p " +
                "RETURN DISTINCT p, rId", query.findByType("ORBITS", 3).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-707
     */
    @Test
    public void testFindAllByTypeCollection() throws Exception {
        assertEquals("MATCH ()-[r0:`ORBITS`]-() WHERE ID(r0) IN {ids}  " +
                        "WITH r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..1]-() " +
                        "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*1..1]-() " +
                        "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH ID(r0) AS rId,startPaths + endPaths  AS paths " +
                        "UNWIND paths AS p RETURN DISTINCT p, rId",
                query.findAllByType("ORBITS", Arrays.asList(1L, 2L, 3L), 1).getStatement());
    }

    @Test
    public void testFindByProperty() throws Exception {
        assertEquals("MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` = { `distance_0` }  " +
                        "WITH r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() " +
                        "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() " +
                        "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH ID(r0) AS rId,startPaths + endPaths  AS paths " +
                        "UNWIND paths AS p RETURN DISTINCT p, rId",
                query.findByType("ORBITS", new Filters().add(new Filter("distance", ComparisonOperator.EQUALS, 60.2)), 4).getStatement());
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
        query.findByType("ORBITS", new Filters().add(new Filter("perihelion", ComparisonOperator.EQUALS, 19.7)), 0).getStatement();
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
        query.findByType("ORBITS", new Filters().add(new Filter("period", ComparisonOperator.EQUALS, 2103.776)), -1).getStatement();
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByNestedPropertyOutgoing() throws Exception {
        Filter planetFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetFilter.setNestedPropertyName("world");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection("OUTGOING");
        assertEquals("MATCH (n:`Planet`) WHERE n.`name` = { `world_name_0` } " +
                        "MATCH (n)-[r0:`ORBITS`]->(m)  WITH r0,startnode(r0) AS n, endnode(r0) AS m " +
                        "MATCH p1 = (n)-[*0..4]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
                        "MATCH p2 = (m)-[*0..4]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH ID(r0) AS rId,startPaths + endPaths  AS paths " +
                        "UNWIND paths AS p RETURN DISTINCT p, rId",
                query.findByType("ORBITS", new Filters().add(planetFilter), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByNestedPropertyIncoming() throws Exception {
        Filter planetFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetFilter.setNestedPropertyName("world");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection("INCOMING");
        assertEquals("MATCH (m:`Planet`) WHERE m.`name` = { `world_name_0` } MATCH (n)-[r0:`ORBITS`]->(m)  " +
                        "WITH r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() " +
                        "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
                        "MATCH p2 = (m)-[*0..4]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId",
                query.findByType("ORBITS", new Filters().add(planetFilter), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByMultipleNestedProperties() throws Exception {
        Filter planetNameFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetNameFilter.setNestedPropertyName("world");
        planetNameFilter.setNestedEntityTypeLabel("Planet");
        planetNameFilter.setRelationshipType("ORBITS");
        planetNameFilter.setRelationshipDirection("OUTGOING");

        Filter planetMoonsFilter = new Filter("moons", ComparisonOperator.EQUALS, "Earth");
        planetMoonsFilter.setNestedPropertyName("moons");
        planetMoonsFilter.setNestedEntityTypeLabel("Planet");
        planetMoonsFilter.setRelationshipType("ORBITS");
        planetMoonsFilter.setRelationshipDirection("OUTGOING");
        planetMoonsFilter.setBooleanOperator(BooleanOperator.AND);

        assertEquals("MATCH (n:`Planet`) WHERE n.`name` = { `world_name_0` } AND n.`moons` = { `moons_moons_1` } " +
                        "MATCH (n)-[r0:`ORBITS`]->(m)  WITH r0,startnode(r0) AS n, endnode(r0) AS m " +
                        "MATCH p1 = (n)-[*0..4]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
                        "MATCH p2 = (m)-[*0..4]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH ID(r0) AS rId,startPaths + endPaths  AS paths " +
                        "UNWIND paths AS p RETURN DISTINCT p, rId",
                query.findByType("ORBITS", new Filters().add(planetNameFilter, planetMoonsFilter), 4).getStatement());
    }


    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByMultipleNestedPropertiesOnBothEnds() throws Exception {
        Filter moonFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");

        moonFilter.setNestedPropertyName("world");
        moonFilter.setNestedEntityTypeLabel("Moon");
        moonFilter.setRelationshipType("ORBITS");
        moonFilter.setRelationshipDirection("OUTGOING");

        Filter planetFilter = new Filter("colour", ComparisonOperator.EQUALS, "Red");
        planetFilter.setNestedPropertyName("colour");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection("INCOMING");

        assertEquals("MATCH (n:`Moon`) WHERE n.`name` = { `world_name_0` } MATCH (m:`Planet`) WHERE m.`colour` = { `colour_colour_1` } " +
                        "MATCH (n)-[r0:`ORBITS`]->(m)  WITH r0,startnode(r0) AS n, endnode(r0) AS m " +
                        "MATCH p1 = (n)-[*0..4]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
                        "MATCH p2 = (m)-[*0..4]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId",
                query.findByType("ORBITS", new Filters().add(moonFilter, planetFilter), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByPropertiesAnded() throws Exception {
        Filter distance = new Filter("distance", ComparisonOperator.EQUALS, 60.2);
        Filter time = new Filter("time", ComparisonOperator.EQUALS, 3600);
        time.setBooleanOperator(BooleanOperator.AND);
        assertEquals("MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` = { `distance_0` } AND r0.`time` = { `time_1` }  " +
                        "WITH r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() " +
                        "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() " +
                        "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId",
                query.findByType("ORBITS", new Filters().add(distance, time), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByPropertiesOred() throws Exception {
        Filter distance = new Filter("distance", ComparisonOperator.EQUALS, 60.2);
        Filter time = new Filter("time", ComparisonOperator.EQUALS, 3600);
        time.setBooleanOperator(BooleanOperator.OR);
        assertEquals("MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` = { `distance_0` } OR r0.`time` = { `time_1` }  " +
                        "WITH r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() " +
                        "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() " +
                        "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId",
                query.findByType("ORBITS", new Filters().add(distance, time), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByPropertiesWithDifferentComparisonOperatorsAnded() throws Exception {
        Filter distance = new Filter("distance", ComparisonOperator.LESS_THAN, 60.2);
        Filter time = new Filter("time", ComparisonOperator.EQUALS, 3600);
        time.setBooleanOperator(BooleanOperator.AND);
        assertEquals("MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` < { `distance_0` } AND r0.`time` = { `time_1` }  " +
                        "WITH r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() " +
                        "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() " +
                        "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId",
                query.findByType("ORBITS", new Filters().add(distance, time), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByPropertiesWithDifferentComparisonOperatorsOred() throws Exception {
        Filter distance = new Filter("distance", ComparisonOperator.EQUALS, 60.2);
        Filter time = new Filter("time", ComparisonOperator.GREATER_THAN, 3600);
        time.setBooleanOperator(BooleanOperator.OR);
        assertEquals("MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` = { `distance_0` } OR r0.`time` > { `time_1` }  " +
                        "WITH r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() " +
                        "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() " +
                        "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId",
                query.findByType("ORBITS", new Filters().add(distance, time), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByBaseAndNestedPropertyOutgoing() throws Exception {
        Filter planetFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetFilter.setNestedPropertyName("world");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection("OUTGOING");
        Filter time = new Filter("time", ComparisonOperator.EQUALS, 3600);
        time.setBooleanOperator(BooleanOperator.AND);
        assertEquals("MATCH (n:`Planet`) WHERE n.`name` = { `world_name_0` } " +
                        "MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`time` = { `time_1` }  " +
                        "WITH r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() " +
                        "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() " +
                        "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId",
                query.findByType("ORBITS", new Filters().add(planetFilter, time), 4).getStatement());
    }


    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByBaseAndNestedPropertyIncoming() throws Exception {
        Filter planetFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetFilter.setNestedPropertyName("world");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection("INCOMING");
        Filter time = new Filter("time", ComparisonOperator.EQUALS, 3600);
        assertEquals("MATCH (m:`Planet`) WHERE m.`name` = { `world_name_0` } " +
                        "MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`time` = { `time_1` }  " +
                        "WITH r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() " +
                        "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() " +
                        "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                        "WITH ID(r0) AS rId,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, rId",
                query.findByType("ORBITS", new Filters().add(planetFilter, time), 4).getStatement());
    }


    /**
     * @throws Exception
     * @see DATAGRAPH-632
     */
    @Test
    public void testFindByBaseAndMultipleNestedPropertiesOnBothEnds() throws Exception {
        Filter moonFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        moonFilter.setNestedPropertyName("world");
        moonFilter.setNestedEntityTypeLabel("Moon");
        moonFilter.setRelationshipType("ORBITS");
        moonFilter.setRelationshipDirection("OUTGOING");

        Filter planetFilter = new Filter("colour", ComparisonOperator.EQUALS, "Red");
        planetFilter.setNestedPropertyName("colour");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection("INCOMING");

        Filter time = new Filter("time", ComparisonOperator.EQUALS, 3600);
        time.setBooleanOperator(BooleanOperator.AND);

        assertEquals("MATCH (n:`Moon`) WHERE n.`name` = { `world_name_0` } MATCH (m:`Planet`) WHERE m.`colour` = { `colour_colour_1` } " +
                "MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`time` = { `time_2` }  " +
                "WITH r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() " +
                "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() " +
                "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH ID(r0) AS rId,startPaths + endPaths  AS paths " +
                "UNWIND paths AS p RETURN DISTINCT p, rId", query.findByType("ORBITS", new Filters().add(moonFilter, planetFilter, time), 4).getStatement());
    }

    /**
     * @throws Exception
     * @see Issue #73
     */
    @Test(expected = MissingOperatorException.class)
    public void testFindByPropertiesAndedWithMissingBooleanOperator() throws Exception {
        Filter distance = new Filter("distance", ComparisonOperator.EQUALS, 60.2);
        Filter time = new Filter("time", ComparisonOperator.EQUALS, 3600);
        query.findByType("ORBITS", new Filters().add(distance, time), 4).getStatement();
    }

    /**
     * @throws Exception
     * @see Issue #73
     */
    @Test(expected = MissingOperatorException.class)
    public void testFindByMultipleNestedPropertiesMissingBooleanOperator() throws Exception {
        Filter planetNameFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetNameFilter.setNestedPropertyName("world");
        planetNameFilter.setNestedEntityTypeLabel("Planet");
        planetNameFilter.setRelationshipType("ORBITS");
        planetNameFilter.setRelationshipDirection("OUTGOING");

        Filter planetMoonsFilter = new Filter("moons", ComparisonOperator.EQUALS, "Earth");
        planetMoonsFilter.setNestedPropertyName("moons");
        planetMoonsFilter.setNestedEntityTypeLabel("Planet");
        planetMoonsFilter.setRelationshipType("ORBITS");
        planetMoonsFilter.setRelationshipDirection("OUTGOING");

        query.findByType("ORBITS", new Filters().add(planetNameFilter, planetMoonsFilter), 4).getStatement();
    }
}
