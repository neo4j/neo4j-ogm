/*
 * Copyright (c) 2002-2025 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.session.request.strategy.impl;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.neo4j.ogm.cypher.ComparisonOperator.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.domain.music.Node;
import org.neo4j.ogm.domain.music.RelBetweenNodes;
import org.neo4j.ogm.exception.core.InvalidDepthException;
import org.neo4j.ogm.exception.core.MissingOperatorException;
import org.neo4j.ogm.session.request.strategy.QueryStatements;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Jasper Blues
 * @author Michael J. Simons
 */
public class RelationshipQueryStatementsTest {

    private final QueryStatements<Long> query = new RelationshipQueryStatements<>();
    private final QueryStatements<String> primaryQuery = new RelationshipQueryStatements<>("uuid",
        new PathRelationshipLoadClauseBuilder());

    @Test
    void testFindOne() {
        assertThat(query.findOne(0L, 2).getStatement()).isEqualTo("MATCH ()-[r0]->() WHERE ID(r0)=$id  " +
            "WITH r0, STARTNODE(r0) AS n, ENDNODE(r0) AS m MATCH p1 = (n)-[*0..2]-() " +
            "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..2]-() " +
            "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
            "WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)");
    }

    @Test
    void testFindOneByType() {
        assertThat(query.findOneByType("ORBITS", 0L, 2).getStatement())
            .isEqualTo("MATCH ()-[r0:`ORBITS`]->() WHERE ID(r0)=$id " +
                "WITH r0,STARTNODE(r0) AS n, ENDNODE(r0) AS m MATCH p1 = (n)-[*0..2]-() " +
                "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..2]-() " +
                "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                "WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)");

        // Also assert that an empty type is the same as the untyped findOne(..)
        /*assertEquals(query.findOneByType("", 0L, 2).getStatement(),
                query.findOne(0L, 2).getStatement());
        assertEquals(query.findOneByType(null, 0L, 2).getStatement(),
                query.findOne(0L, 2).getStatement());*/
    }

    @Test
    void testFindOneByTypePrimaryIndex() {
        PagingAndSortingQuery pagingAndSortingQuery = primaryQuery.findOneByType("ORBITS", "test-uuid", 2);
        assertThat(pagingAndSortingQuery.getStatement())
            .isEqualTo("MATCH ()-[r0:`ORBITS`]->() WHERE r0.`uuid`=$id  " +
                "WITH r0, STARTNODE(r0) AS n, ENDNODE(r0) AS m MATCH p1 = (n)-[*0..2]-() " +
                "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..2]-() " +
                "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                "WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)");

        assertThat(pagingAndSortingQuery.getParameters()).contains(entry("id", "test-uuid"));
    }

    @Test
    void testFindByLabel() {
        assertThat(query.findByType("ORBITS", 3).getStatement())
            .isEqualTo("MATCH ()-[r0:`ORBITS`]-()  WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m " +
                "MATCH p1 = (n)-[*0..3]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m " +
                "MATCH p2 = (m)-[*0..3]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                "WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p " +
                "RETURN DISTINCT p, ID(r0)");
    }

    // DATAGRAPH-707
    @Test
    void testFindAllByTypeCollection() {
        assertThat(query.findAllByType("ORBITS", asList(1L, 2L, 3L), 1).getStatement())
            .isEqualTo("MATCH ()-[r0:`ORBITS`]-() WHERE ID(r0) IN $ids  " +
                "WITH DISTINCT(r0) as r0, startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..1]-() " +
                "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..1]-() " +
                "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                "WITH r0,startPaths + endPaths  AS paths " +
                "UNWIND paths AS p RETURN DISTINCT p, ID(r0)");
    }

    @Test
    void testFindAllByTypePrimaryIndex() {
        PagingAndSortingQuery pagingAndSortingQuery = primaryQuery
            .findAllByType("ORBITS", Arrays.asList("test-uuid-1", "test-uuid-2"), 2);

        assertThat(pagingAndSortingQuery.getStatement())
            .isEqualTo("MATCH ()-[r0:`ORBITS`]-() WHERE r0.`uuid` IN $ids  " +
                "WITH DISTINCT(r0) as r0, startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..2]-() " +
                "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..2]-() " +
                "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                "WITH r0,startPaths + endPaths  AS paths " +
                "UNWIND paths AS p RETURN DISTINCT p, ID(r0)");

        assertThat(pagingAndSortingQuery.getParameters()).contains(entry("ids", Arrays.asList("test-uuid-1", "test-uuid-2")));
    }

    @Test
    void testFindByProperty() {
        assertThat(
            query.findByType("ORBITS", new Filters().add(new Filter("distance", EQUALS, 60.2)), 4).getStatement())
            .isEqualTo("MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` = $`distance_0`  " +
                "WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() " +
                "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() " +
                "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                "WITH r0,startPaths + endPaths  AS paths " +
                "UNWIND paths AS p RETURN DISTINCT p, ID(r0)");
    }

    @Test
    void testFindOneZeroDepth() throws Exception {
        assertThrows(InvalidDepthException.class, () -> {
            query.findOne(0L, 0).getStatement();
        });
    }

    @Test
    void testFindByLabelZeroDepth() throws Exception {
        assertThrows(InvalidDepthException.class, () -> {
            query.findByType("ORBITS", 0).getStatement();
        });
    }

    @Test
    void testFindByPropertyZeroDepth() throws Exception {
        assertThrows(InvalidDepthException.class, () -> {
            query.findByType("ORBITS", new Filters().add(new Filter("perihelion", ComparisonOperator.EQUALS, 19.7)), 0)
                .getStatement();
        });
    }

    @Test
    void testFindOneInfiniteDepth() throws Exception {
        assertThrows(InvalidDepthException.class, () -> {
            query.findOne(0L, -1).getStatement();
        });
    }

    @Test
    void testFindByLabelInfiniteDepth() throws Exception {
        assertThrows(InvalidDepthException.class, () -> {
            query.findByType("ORBITS", -1).getStatement();
        });
    }

    @Test
    void testFindByPropertyInfiniteDepth() throws Exception {
        assertThrows(InvalidDepthException.class, () -> {
            query.findByType("ORBITS", new Filters().add(new Filter("period", ComparisonOperator.EQUALS, 2103.776)), -1)
                .getStatement();
        });
    }

    // DATAGRAPH-632
    @Test
    void testFindByNestedPropertyOutgoing() {
        Filter planetFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetFilter.setNestedPropertyName("world");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection(Relationship.Direction.OUTGOING);
        assertThat(query.findByType("ORBITS", new Filters().add(planetFilter), 4).getStatement())
            .isEqualTo("""
                MATCH (n:`Planet`)-[r0:`ORBITS`]->(m) WHERE ( n.`name` = $`world_name_0` ) \
                WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m \
                MATCH p1 = (n)-[*0..4]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m \
                MATCH p2 = (m)-[*0..4]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths \
                WITH r0,startPaths + endPaths  AS paths \
                UNWIND paths AS p RETURN DISTINCT p, ID(r0)""");
    }

    // DATAGRAPH-632
    @Test
    void testFindByNestedPropertyIncoming() {
        Filter planetFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetFilter.setNestedPropertyName("world");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection(Relationship.Direction.INCOMING);
        assertThat(query.findByType("ORBITS", new Filters().add(planetFilter), 4).getStatement())
            .isEqualTo("""
                MATCH (n)<-[r0:`ORBITS`]-(m:`Planet`) WHERE ( m.`name` = $`world_name_0` ) \
                WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() \
                WITH r0, COLLECT(DISTINCT p1) AS startPaths, m \
                MATCH p2 = (m)-[*0..4]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths \
                WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)""");
    }

    // DATAGRAPH-632
    @Test
    void testFindByMultipleNestedProperties() {
        Filter planetNameFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetNameFilter.setNestedPropertyName("world");
        planetNameFilter.setNestedEntityTypeLabel("Planet");
        planetNameFilter.setRelationshipType("ORBITS");
        planetNameFilter.setRelationshipDirection(Relationship.Direction.OUTGOING);

        Filter planetMoonsFilter = new Filter("moons", ComparisonOperator.EQUALS, "Earth");
        planetMoonsFilter.setNestedPropertyName("moons");
        planetMoonsFilter.setNestedEntityTypeLabel("Planet");
        planetMoonsFilter.setRelationshipType("ORBITS");
        planetMoonsFilter.setRelationshipDirection(Relationship.Direction.OUTGOING);
        planetMoonsFilter.setBooleanOperator(BooleanOperator.AND);

        assertThat(query.findByType("ORBITS", new Filters().add(planetNameFilter, planetMoonsFilter), 4).getStatement())
            .isEqualTo("""
                MATCH (n:`Planet`)-[r0:`ORBITS`]->(m) WHERE ( n.`name` = $`world_name_0` AND n.`moons` = $`moons_moons_1` ) \
                WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m \
                MATCH p1 = (n)-[*0..4]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m \
                MATCH p2 = (m)-[*0..4]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths \
                WITH r0,startPaths + endPaths  AS paths \
                UNWIND paths AS p RETURN DISTINCT p, ID(r0)""");
    }

    // DATAGRAPH-632
    @Test
    void testFindByMultipleNestedPropertiesOnBothEnds() {
        Filter moonFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");

        moonFilter.setNestedPropertyName("world");
        moonFilter.setNestedEntityTypeLabel("Moon");
        moonFilter.setRelationshipType("ORBITS");
        moonFilter.setRelationshipDirection(Relationship.Direction.OUTGOING);

        Filter planetFilter = new Filter("colour", ComparisonOperator.EQUALS, "Red");
        planetFilter.setNestedPropertyName("colour");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection(Relationship.Direction.INCOMING);

        assertThat(query.findByType("ORBITS", new Filters().add(moonFilter, planetFilter), 4).getStatement()).isEqualTo("""
                MATCH (n:`Moon`)-[r0:`ORBITS`]->(m:`Planet`) WHERE ( n.`name` = $`world_name_0` AND m.`colour` = $`colour_colour_1` ) \
                WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m \
                MATCH p1 = (n)-[*0..4]-() WITH r0, COLLECT(DISTINCT p1) AS startPaths, m \
                MATCH p2 = (m)-[*0..4]-() WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths \
                WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)""");
    }

    // DATAGRAPH-632
    @Test
    void testFindByPropertiesAnded() {
        Filter distance = new Filter("distance", ComparisonOperator.EQUALS, 60.2);
        Filter time = new Filter("time", ComparisonOperator.EQUALS, 3600);
        time.setBooleanOperator(BooleanOperator.AND);
        assertThat(query.findByType("ORBITS", new Filters().add(distance, time), 4).getStatement()).isEqualTo(
            "MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` = $`distance_0` AND r0.`time` = $`time_1`  " +
                "WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() " +
                "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() " +
                "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                "WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)");
    }

    // DATAGRAPH-632
    @Test
    void testFindByPropertiesOred() {
        Filter distance = new Filter("distance", ComparisonOperator.EQUALS, 60.2);
        Filter time = new Filter("time", ComparisonOperator.EQUALS, 3600);
        time.setBooleanOperator(BooleanOperator.OR);
        assertThat(query.findByType("ORBITS", new Filters().add(distance, time), 4).getStatement()).isEqualTo(
            "MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` = $`distance_0` OR r0.`time` = $`time_1`  " +
                "WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() " +
                "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() " +
                "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                "WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)");
    }

    // DATAGRAPH-632
    @Test
    void testFindByPropertiesWithDifferentComparisonOperatorsAnded() {
        Filter distance = new Filter("distance", ComparisonOperator.LESS_THAN, 60.2);
        Filter time = new Filter("time", ComparisonOperator.EQUALS, 3600);
        time.setBooleanOperator(BooleanOperator.AND);
        assertThat(query.findByType("ORBITS", new Filters().add(distance, time), 4).getStatement()).isEqualTo(
            "MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` < $`distance_0` AND r0.`time` = $`time_1`  " +
                "WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() " +
                "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() " +
                "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                "WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)");
    }

    // DATAGRAPH-632
    @Test
    void testFindByPropertiesWithDifferentComparisonOperatorsOred() {
        Filter distance = new Filter("distance", ComparisonOperator.EQUALS, 60.2);
        Filter time = new Filter("time", ComparisonOperator.GREATER_THAN, 3600);
        time.setBooleanOperator(BooleanOperator.OR);
        assertThat(query.findByType("ORBITS", new Filters().add(distance, time), 4).getStatement()).isEqualTo(
            "MATCH (n)-[r0:`ORBITS`]->(m) WHERE r0.`distance` = $`distance_0` OR r0.`time` > $`time_1`  " +
                "WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() " +
                "WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() " +
                "WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths " +
                "WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)");
    }

    // DATAGRAPH-632
    @Test
    void testFindByBaseAndNestedPropertyOutgoing() {
        Filter planetFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetFilter.setNestedPropertyName("world");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection(Relationship.Direction.OUTGOING);
        Filter time = new Filter("time", ComparisonOperator.EQUALS, 3600);
        time.setBooleanOperator(BooleanOperator.AND);
        assertThat(query.findByType("ORBITS", new Filters().add(planetFilter, time), 4).getStatement())
            .isEqualTo("""
                MATCH (n:`Planet`)-[r0:`ORBITS`]->(m) WHERE ( n.`name` = $`world_name_0` ) AND r0.`time` = $`time_1`  \
                WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() \
                WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() \
                WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths \
                WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)""");
    }

    // DATAGRAPH-632
    @Test
    void testFindByBaseAndNestedPropertyIncoming() {
        Filter planetFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetFilter.setNestedPropertyName("world");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection(Relationship.Direction.INCOMING);
        Filter time = new Filter("time", ComparisonOperator.EQUALS, 3600);
        assertThat(query.findByType("ORBITS", new Filters().add(planetFilter, time), 4).getStatement())
            .isEqualTo("""
                MATCH (n)<-[r0:`ORBITS`]-(m:`Planet`) WHERE ( m.`name` = $`world_name_0` ) AND r0.`time` = $`time_1`  \
                WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() \
                WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() \
                WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths \
                WITH r0,startPaths + endPaths  AS paths UNWIND paths AS p RETURN DISTINCT p, ID(r0)""");
    }

    // DATAGRAPH-632
    @Test
    void testFindByBaseAndMultipleNestedPropertiesOnBothEnds() {
        Filter moonFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        moonFilter.setNestedPropertyName("world");
        moonFilter.setNestedEntityTypeLabel("Moon");
        moonFilter.setRelationshipType("ORBITS");
        moonFilter.setRelationshipDirection(Relationship.Direction.OUTGOING);

        Filter planetFilter = new Filter("colour", ComparisonOperator.EQUALS, "Red");
        planetFilter.setNestedPropertyName("colour");
        planetFilter.setNestedEntityTypeLabel("Planet");
        planetFilter.setRelationshipType("ORBITS");
        planetFilter.setRelationshipDirection(Relationship.Direction.INCOMING);

        Filter time = new Filter("time", ComparisonOperator.EQUALS, 3600);
        time.setBooleanOperator(BooleanOperator.AND);

        assertThat(query.findByType("ORBITS", new Filters().add(moonFilter, planetFilter, time), 4).getStatement())
            .isEqualTo("""
                MATCH (n:`Moon`)-[r0:`ORBITS`]->(m:`Planet`) WHERE ( n.`name` = $`world_name_0` AND m.`colour` = $`colour_colour_1` ) AND r0.`time` = $`time_2`  \
                WITH DISTINCT(r0) as r0,startnode(r0) AS n, endnode(r0) AS m MATCH p1 = (n)-[*0..4]-() \
                WITH r0, COLLECT(DISTINCT p1) AS startPaths, m MATCH p2 = (m)-[*0..4]-() \
                WITH r0, startPaths, COLLECT(DISTINCT p2) AS endPaths WITH r0,startPaths + endPaths  AS paths \
                UNWIND paths AS p RETURN DISTINCT p, ID(r0)""");
    }

    // GH-73
    @Test
    void testFindByPropertiesAndedWithMissingBooleanOperator() {
        assertThrows(MissingOperatorException.class, () -> {
            Filter distance = new Filter("distance", ComparisonOperator.EQUALS, 60.2);
            Filter time = new Filter("time", ComparisonOperator.EQUALS, 3600);
            query.findByType("ORBITS", new Filters().add(distance, time), 4).getStatement();
        });
    }

    // GH-73
    @Test
    void testFindByMultipleNestedPropertiesMissingBooleanOperator() {
        assertThrows(MissingOperatorException.class, () -> {
            Filter planetNameFilter = new Filter("name", ComparisonOperator.EQUALS, "Earth");
            planetNameFilter.setNestedPropertyName("world");
            planetNameFilter.setNestedEntityTypeLabel("Planet");
            planetNameFilter.setRelationshipType("ORBITS");
            planetNameFilter.setRelationshipDirection(Relationship.Direction.OUTGOING);

            Filter planetMoonsFilter = new Filter("moons", ComparisonOperator.EQUALS, "Earth");
            planetMoonsFilter.setNestedPropertyName("moons");
            planetMoonsFilter.setNestedEntityTypeLabel("Planet");
            planetMoonsFilter.setRelationshipType("ORBITS");
            planetMoonsFilter.setRelationshipDirection(Relationship.Direction.OUTGOING);

            query.findByType("ORBITS", new Filters().add(planetNameFilter, planetMoonsFilter), 4).getStatement();
        });
    }
}
