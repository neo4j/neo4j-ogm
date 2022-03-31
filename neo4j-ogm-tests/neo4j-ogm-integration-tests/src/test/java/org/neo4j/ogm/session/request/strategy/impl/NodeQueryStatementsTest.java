/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
import static org.neo4j.ogm.cypher.ComparisonOperator.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.function.DistanceComparison;
import org.neo4j.ogm.cypher.function.DistanceFromPoint;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.exception.core.MissingOperatorException;
import org.neo4j.ogm.session.request.strategy.QueryStatements;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Jasper Blues
 * author Michael J. Simons
 */
public class NodeQueryStatementsTest {

    /**
     * QueryStatements with graph id
     */
    private final QueryStatements<Long> queryStatements = new NodeQueryStatements<>();

    /**
     * QueryStatements with primary index property
     */
    private final QueryStatements<String> primaryQueryStatements = new NodeQueryStatements<>("uuid",
        new PathNodeLoadClauseBuilder());

    @Test
    public void testFindOne() {
        assertThat(queryStatements.findOne(0L, 2).getStatement())
            .isEqualTo("MATCH (n) WHERE ID(n) = $id WITH n MATCH p=(n)-[*0..2]-(m) RETURN p");
    }

    @Test
    public void testFindOnePrimaryIndex() {
        PagingAndSortingQuery query = primaryQueryStatements.findOne("test-uuid", 2);
        assertThat(query.getStatement())
            .isEqualTo("MATCH (n) WHERE n.`uuid` = $id WITH n MATCH p=(n)-[*0..2]-(m) RETURN p");
        assertThat(query.getParameters()).containsEntry("id", "test-uuid");
    }

    @Test
    public void testFindOneByType() {
        PagingAndSortingQuery query = queryStatements.findOneByType("Orbit", 0L, 2);
        assertThat(query.getStatement())
            .isEqualTo("MATCH (n:`Orbit`) WHERE ID(n) = $id WITH n MATCH p=(n)-[*0..2]-(m) RETURN p");
        assertThat(query.getParameters()).containsEntry("id", 0L);

        // Also assert that an empty label is the same as using the typeless variant
        assertThat(queryStatements.findOne(0L, 2).getStatement())
            .isEqualTo(queryStatements.findOneByType("", 0L, 2).getStatement());
        assertThat(queryStatements.findOne(0L, 2).getStatement())
            .isEqualTo(queryStatements.findOneByType(null, 0L, 2).getStatement());
    }

    @Test
    public void testFindOneByTypePrimaryIndex() {
        PagingAndSortingQuery query = primaryQueryStatements.findOneByType("Orbit", "test-uuid", 2);

        assertThat(query.getStatement())
            .isEqualTo("MATCH (n:`Orbit`) WHERE n.`uuid` = $id WITH n MATCH p=(n)-[*0..2]-(m) RETURN p");
        assertThat(query.getParameters()).containsEntry("id", "test-uuid");
    }

    @Test
    public void testFindOneByTypePrimaryIndexInfiniteDepth() {
        PagingAndSortingQuery query = primaryQueryStatements.findOneByType("Orbit", "test-uuid", -1);

        assertThat(query.getStatement())
            .isEqualTo("MATCH (n:`Orbit`) WHERE n.`uuid` = $id WITH n MATCH p=(n)-[*0..]-(m) RETURN p");
        assertThat(query.getParameters()).containsEntry("id", "test-uuid");
    }

    @Test
    public void testFindByLabel() {
        String statement = queryStatements.findByType("Orbit", 3).getStatement();
        assertThat(statement).isEqualTo("MATCH (n:`Orbit`) WITH n MATCH p=(n)-[*0..3]-(m) RETURN p");
    }

    @Test
    public void testFindByDistance() {
        DistanceComparison function = new DistanceComparison(new DistanceFromPoint(37.4d, 112.9d, 1000.0d));
        Filters filters = new Filters().add(new Filter(function, ComparisonOperator.EQUALS));
        String statement = queryStatements.findByType("Restaurant",
            filters, 4).getStatement();
        assertThat(statement).isEqualTo(
            "MATCH (n:`Restaurant`) WHERE point.distance(point({latitude: n.latitude, longitude: n.longitude}),point({latitude: $lat, longitude: $lon})) = $distance WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)");
    }

    @Test
    public void testFindByPropertyIsNull() {

        Filter isNull = new Filter("score", ComparisonOperator.IS_NULL, null);

        String statement = queryStatements.findByType("Restaurant", new Filters().add(isNull), 3).getStatement();
        assertThat(statement)
            .isEqualTo("MATCH (n:`Restaurant`) WHERE n.`score` IS NULL WITH n MATCH p=(n)-[*0..3]-(m) RETURN p, ID(n)");

        Filter isNotNull = new Filter("score", ComparisonOperator.IS_NULL, null);
        isNotNull.setNegated(true);

        statement = queryStatements.findByType("Restaurant", new Filters().add(isNotNull), 3).getStatement();
        assertThat(statement).isEqualTo(
            "MATCH (n:`Restaurant`) WHERE NOT(n.`score` IS NULL ) WITH n MATCH p=(n)-[*0..3]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-707
    public void testFindAllByLabel() {
        assertThat(queryStatements.findAllByType("Orbit", asList(1L, 2L, 3L), 0).getStatement())
            .isEqualTo("MATCH (n:`Orbit`) WHERE ID(n) IN $ids WITH n RETURN n");
    }

    @Test
    public void testFindAllByLabelPrimaryIndex() {
        List<String> ids = Arrays.asList("uuid-1", "uuid-2");
        PagingAndSortingQuery query = primaryQueryStatements.findAllByType("Orbit", ids, 0);

        assertThat(query.getStatement())
            .isEqualTo("MATCH (n:`Orbit`) WHERE n.`uuid` IN $ids WITH n RETURN n");
        assertThat(query.getParameters())
            .containsEntry("ids", ids);
    }

    @Test
    public void testFindAllByLabelPrimaryIndexInfiniteDepth() {
        List<String> ids = Arrays.asList("uuid-1", "uuid-2");
        PagingAndSortingQuery query = primaryQueryStatements.findAllByType("Orbit", ids, -1);

        assertThat(query.getStatement())
            .isEqualTo("MATCH (n:`Orbit`) WHERE n.`uuid` IN $ids WITH n MATCH p=(n)-[*0..]-(m) RETURN p");
        assertThat(query.getParameters())
            .containsEntry("ids", ids);
    }

    @Test // DATAGRAPH-707
    public void testFindAllByLabelDepthOne() {
        assertThat(queryStatements.findAllByType("Orbit", asList(1L, 2L, 3L), 1).getStatement())
            .isEqualTo("MATCH (n:`Orbit`) WHERE ID(n) IN $ids WITH n MATCH p=(n)-[*0..1]-(m) RETURN p");
    }

    @Test // DATAGRAPH-707
    public void testFindAllByLabelDepthInfinity() {
        assertThat(queryStatements.findAllByType("Orbit", asList(1L, 2L, 3L), -1).getStatement())
            .isEqualTo("MATCH (n:`Orbit`) WHERE ID(n) IN $ids WITH n MATCH p=(n)-[*0..]-(m) RETURN p");
    }

    @Test
    public void testFindByProperty() {
        String statement = queryStatements.findByType("Asteroid",
            new Filters().add(new Filter("diameter", ComparisonOperator.EQUALS, 60.2)), 4).getStatement();
        assertThat(statement).isEqualTo(
            "MATCH (n:`Asteroid`) WHERE n.`diameter` = $`diameter_0` WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)");
    }

    @Test
    public void testFindOneZeroDepth() {
        assertThat(queryStatements.findOne(0L, 0).getStatement())
            .isEqualTo("MATCH (n) WHERE ID(n) = $id WITH n RETURN n");
    }

    @Test
    public void testFindOneZeroDepthPrimaryIndex() {
        PagingAndSortingQuery query = primaryQueryStatements.findOne("test-uuid", 0);

        assertThat(query.getStatement()).isEqualTo("MATCH (n) WHERE n.`uuid` = $id WITH n RETURN n");
    }

    @Test
    public void testFindByLabelZeroDepth() {
        assertThat(queryStatements.findByType("Orbit", 0).getStatement())
            .isEqualTo("MATCH (n:`Orbit`) WITH n RETURN n");
    }

    @Test
    public void testFindByPropertyZeroDepth() {
        assertThat(queryStatements.findByType("Asteroid", new Filters().add(new Filter("diameter", EQUALS, 60.2)), 0)
            .getStatement()).isEqualTo("MATCH (n:`Asteroid`) WHERE n.`diameter` = $`diameter_0` WITH n RETURN n");
    }

    @Test // DATAGRAPH-781
    public void testFindByPropertyWithInfiniteValue() {
        PagingAndSortingQuery pagingAndSortingQuery = queryStatements
            .findByType("Asteroid", new Filters().add(new Filter("albedo", ComparisonOperator.EQUALS, -12.2)), 0);

        assertThat(pagingAndSortingQuery.getStatement())
            .isEqualTo("MATCH (n:`Asteroid`) WHERE n.`albedo` = $`albedo_0` WITH n RETURN n");
        assertThat((double) pagingAndSortingQuery.getParameters().get("albedo_0")).isEqualTo(-12.2, within(0.005));
    }

    @Test // DATAGRAPH-589
    public void testFindByLabelWithIllegalCharacters() {
        assertThat(queryStatements.findByType("l'artiste", 3).getStatement())
            .isEqualTo("MATCH (n:`l'artiste`) WITH n MATCH p=(n)-[*0..3]-(m) RETURN p");
    }

    @Test // DATAGRAPH-595
    public void testFindOneInfiniteDepth() {
        assertThat(queryStatements.findOne(0L, -1).getStatement())
            .isEqualTo("MATCH (n) WHERE ID(n) = $id WITH n MATCH p=(n)-[*0..]-(m) RETURN p");
    }

    @Test // DATAGRAPH-595
    public void testFindByLabelInfiniteDepth() {
        assertThat(queryStatements.findByType("Orbit", -1).getStatement())
            .isEqualTo("MATCH (n:`Orbit`) WITH n MATCH p=(n)-[*0..]-(m) RETURN p");
    }

    @Test // DATAGRAPH-595
    public void testFindByPropertyInfiniteDepth() {
        assertThat(queryStatements.findByType("Asteroid", new Filters().add(new Filter("diameter", EQUALS, 60.2)), -1)
            .getStatement()).isEqualTo(
            "MATCH (n:`Asteroid`) WHERE n.`diameter` = $`diameter_0` WITH n MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-631
    public void testFindByPropertyWithIllegalCharacters() {
        assertThat(queryStatements
            .findByType("Studio", new Filters().add(new Filter("studio-name", EQUALS, "Abbey Road Studios")), 3)
            .getStatement()).isEqualTo(
            "MATCH (n:`Studio`) WHERE n.`studio-name` = $`studio-name_0` WITH n MATCH p=(n)-[*0..3]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-629
    public void testFindByPropertyGreaterThan() {
        Filter parameter = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);
        assertThat(queryStatements.findByType("Asteroid", new Filters().add(parameter), 4).getStatement()).isEqualTo(
            "MATCH (n:`Asteroid`) WHERE n.`diameter` > $`diameter_0` WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-904
    public void testFindByPropertyGreaterThanEqual() {
        Filter parameter = new Filter("diameter", ComparisonOperator.GREATER_THAN_EQUAL, 60);
        assertThat(queryStatements.findByType("Asteroid", new Filters().add(parameter), 4).getStatement()).isEqualTo(
            "MATCH (n:`Asteroid`) WHERE n.`diameter` >= $`diameter_0` WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-629
    public void testFindByPropertyLessThan() {
        Filter parameter = new Filter("diameter", ComparisonOperator.LESS_THAN, 60);
        assertThat(queryStatements.findByType("Asteroid",
            new Filters().add(parameter), 4).getStatement())
            .isEqualTo("MATCH (n:`Asteroid`) WHERE n.`diameter` < $`diameter_0` " +
                "WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-904
    public void testFindByPropertyLessThanEqual() {
        Filter parameter = new Filter("diameter", ComparisonOperator.LESS_THAN_EQUAL, 60);
        assertThat(queryStatements.findByType("Asteroid",
            new Filters().add(parameter), 4).getStatement())
            .isEqualTo("MATCH (n:`Asteroid`) WHERE n.`diameter` <= $`diameter_0` " +
                "WITH n MATCH p=(n)-[*0..4]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-445
    public void testFindByChainedAndedProperties() {
        Filter planetParam = collidesWithEarthFilter();

        Filter moonParam = orbitsMoonFilter();

        assertThat(queryStatements.findByType("Asteroid",
            new Filters().add(planetParam).and(moonParam), 1).getStatement())
            .isEqualTo("MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = $`collidesWith_name_0` " +
                "MATCH (m1:`Moon`) WHERE m1.`name` = $`moon_name_1` MATCH (n)-[:`COLLIDES`]->(m0) " +
                "MATCH (n)<-[:`ORBITS`]-(m1) WITH DISTINCT n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-445
    public void testFindByChainedOredProperties() {
        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");

        Filter moonParam = new Filter("name", ComparisonOperator.EQUALS, "Moon");

        assertThat(queryStatements.findByType("Asteroid",
            new Filters().add(planetParam).or(moonParam), 1).getStatement())
            .isEqualTo("MATCH (n:`Asteroid`) WHERE n.`name` = $`name_0` " +
                "OR n.`name` = $`name_1` WITH n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-629
    public void testFindByMultipleAndPropertiesLessThan() {
        Filter nameParam = new Filter("name", ComparisonOperator.EQUALS, "AST-1");
        Filter diameterParam = new Filter("diameter", ComparisonOperator.LESS_THAN, 60);
        diameterParam.setBooleanOperator(BooleanOperator.AND);
        assertThat(queryStatements.findByType("Asteroid",
            new Filters().add(nameParam).add(diameterParam), 2).getStatement())
            .isEqualTo("MATCH (n:`Asteroid`) WHERE n.`name` = $`name_0` AND n.`diameter` < $`diameter_1` " +
                "WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-629
    public void testFindByMultipleAndPropertiesGreaterThan() {
        Filter nameParam = new Filter("name", ComparisonOperator.EQUALS, "AST-1");
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);
        diameterParam.setBooleanOperator(BooleanOperator.AND);
        assertThat(
            queryStatements.findByType("Asteroid", new Filters().add(nameParam).add(diameterParam), 2).getStatement())
            .isEqualTo(
                "MATCH (n:`Asteroid`) WHERE n.`name` = $`name_0` AND n.`diameter` > $`diameter_1` WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-629
    public void testFindByMultipleAndPropertiesGreaterThanWithDifferentOrder() {
        Filter nameParam = new Filter("name", ComparisonOperator.EQUALS, "AST-1");
        nameParam.setBooleanOperator(BooleanOperator.AND);
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);
        assertThat(
            queryStatements.findByType("Asteroid", new Filters().add(diameterParam).add(nameParam), 2).getStatement())
            .isEqualTo(
                "MATCH (n:`Asteroid`) WHERE n.`diameter` > $`diameter_0` AND n.`name` = $`name_1` WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-629
    public void testFindByMultipleOrPropertiesGreaterThan() {
        Filter nameParam = new Filter("name", ComparisonOperator.EQUALS, "AST-1");
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);
        diameterParam.setBooleanOperator(BooleanOperator.OR);
        assertThat(
            queryStatements.findByType("Asteroid", new Filters().add(nameParam).add(diameterParam), 2).getStatement())
            .isEqualTo(
                "MATCH (n:`Asteroid`) WHERE n.`name` = $`name_0` OR n.`diameter` > $`diameter_1` WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-629
    public void testFindByMultipleOrPropertiesLessThan() {
        Filter nameParam = new Filter("name", ComparisonOperator.EQUALS, "AST-1");
        Filter diameterParam = new Filter("diameter", ComparisonOperator.LESS_THAN, 60);
        diameterParam.setBooleanOperator(BooleanOperator.OR);
        assertThat(
            queryStatements.findByType("Asteroid", new Filters().add(nameParam).add(diameterParam), 2).getStatement())
            .isEqualTo(
                "MATCH (n:`Asteroid`) WHERE n.`name` = $`name_0` OR n.`diameter` < $`diameter_1` WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-629
    public void testFindByMultipleOrPropertiesGreaterThanWithDifferentOrder() {
        Filter nameParam = new Filter("name", ComparisonOperator.EQUALS, "AST-1");
        nameParam.setBooleanOperator(BooleanOperator.OR);
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);
        assertThat(
            queryStatements.findByType("Asteroid", new Filters().add(diameterParam).add(nameParam), 2).getStatement())
            .isEqualTo(
                "MATCH (n:`Asteroid`) WHERE n.`diameter` > $`diameter_0` OR n.`name` = $`name_1` WITH n MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-629
    public void testFindByNestedPropertyOutgoing() {
        Filter planetParam = collidesWithEarthFilter();
        assertThat(queryStatements.findByType("Asteroid", new Filters().add(planetParam), 1).getStatement()).isEqualTo(
            "MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = $`collidesWith_name_0` MATCH (n)-[:`COLLIDES`]->(m0) WITH DISTINCT n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-904
    public void testFindByNestedPropertySameEntityType() {
        Filter filter = new Filter("name", ComparisonOperator.EQUALS, "Vesta");
        filter.setNestedPropertyName("collidesWith");
        filter.setNestedEntityTypeLabel("Asteroid");
        filter.setRelationshipType("COLLIDES");
        filter.setRelationshipDirection(Relationship.Direction.OUTGOING);
        assertThat(queryStatements.findByType("Asteroid", new Filters().add(filter), 1).getStatement()).isEqualTo(
            "MATCH (n:`Asteroid`) MATCH (m0:`Asteroid`) WHERE m0.`name` = $`collidesWith_name_0` MATCH (n)-[:`COLLIDES`]->(m0) WITH DISTINCT n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-629
    public void testFindByNestedPropertyIncoming() {
        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection(Relationship.Direction.INCOMING);
        assertThat(queryStatements.findByType("Asteroid", new Filters().add(planetParam), 1).getStatement()).isEqualTo(
            "MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = $`collidesWith_name_0` MATCH (n)<-[:`COLLIDES`]-(m0) WITH DISTINCT n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-629
    public void testFindByNestedPropertyUndirected() {
        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection(Relationship.Direction.UNDIRECTED);
        assertThat(queryStatements.findByType("Asteroid", new Filters().add(planetParam), 1).getStatement()).isEqualTo(
            "MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = $`collidesWith_name_0` MATCH (n)-[:`COLLIDES`]-(m0) WITH DISTINCT n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-629
    public void testFindByMultipleNestedProperties() {
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);

        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setBooleanOperator(BooleanOperator.AND);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection(Relationship.Direction.OUTGOING);
        assertThat(
            queryStatements.findByType("Asteroid", new Filters().add(diameterParam).add(planetParam), 1).getStatement())
            .isEqualTo(
                "MATCH (n:`Asteroid`) WHERE n.`diameter` > $`diameter_0` MATCH (m0:`Planet`) WHERE m0.`name` = $`collidesWith_name_1` MATCH (n)-[:`COLLIDES`]->(m0) WITH DISTINCT n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-629
    public void testFindByMultipleNestedPropertiesInfiniteDepth() {
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);

        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setBooleanOperator(BooleanOperator.AND);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection(Relationship.Direction.OUTGOING);
        assertThat(queryStatements.findByType("Asteroid", new Filters().add(diameterParam).add(planetParam), -1)
            .getStatement()).isEqualTo(
            "MATCH (n:`Asteroid`) WHERE n.`diameter` > $`diameter_0` MATCH (m0:`Planet`) WHERE m0.`name` = $`collidesWith_name_1` MATCH (n)-[:`COLLIDES`]->(m0) WITH DISTINCT n MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)");
    }

    @Test(expected = UnsupportedOperationException.class) // DATAGRAPH-662
    public void testFindByMultipleNestedPropertiesOred() {
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);

        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setBooleanOperator(BooleanOperator.OR);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection(Relationship.Direction.OUTGOING);

        queryStatements.findByType("Asteroid", new Filters().add(diameterParam).add(planetParam), 1)
            .getStatement();
    }

    @Test(expected = UnsupportedOperationException.class) // DATAGRAPH-662
    public void testFindByMultipleNestedPropertiesOredDepth0() {
        Filter diameterParam = new Filter("diameter", ComparisonOperator.GREATER_THAN, 60);

        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setBooleanOperator(BooleanOperator.OR);
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection(Relationship.Direction.OUTGOING);

        String statement = queryStatements.findByType("Asteroid", new Filters().add(diameterParam).add(planetParam), 0)
            .getStatement();
    }

    @Test // DATAGRAPH-632
    public void testFindByNestedREProperty() {
        Filter planetParam = new Filter("totalDestructionProbability", ComparisonOperator.EQUALS, "20");
        planetParam.setNestedPropertyName("collision");
        planetParam.setNestedEntityTypeLabel("Collision"); //Collision is an RE
        planetParam.setNestedRelationshipEntity(true);
        planetParam.setRelationshipType("COLLIDES"); //assume COLLIDES is the RE type
        planetParam.setRelationshipDirection(Relationship.Direction.OUTGOING);
        planetParam.setNestedRelationshipEntity(true);

        assertThat(queryStatements.findByType("Asteroid", new Filters().add(planetParam), 1).getStatement())
            .isEqualTo("MATCH (n:`Asteroid`) MATCH (n)-[r0:`COLLIDES`]->(m0) " +
                "WHERE r0.`totalDestructionProbability` = $`collision_totalDestructionProbability_0` " +
                "WITH DISTINCT n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    @Test // GH-279
    public void testFindByMultipleNestedREProperty() {
        Filter planetParam = new Filter("totalDestructionProbability", ComparisonOperator.EQUALS, "20");
        planetParam.setNestedPropertyName("collision");
        planetParam.setNestedEntityTypeLabel("Collision"); //Collision is an RE
        planetParam.setNestedRelationshipEntity(true);
        planetParam.setRelationshipType("COLLIDES"); //assume COLLIDES is the RE type
        planetParam.setRelationshipDirection(Relationship.Direction.OUTGOING);
        planetParam.setNestedRelationshipEntity(true);

        Filter satelliteParam = new Filter("signalStrength", ComparisonOperator.GREATER_THAN_EQUAL, "400");
        satelliteParam.setBooleanOperator(BooleanOperator.AND);
        satelliteParam.setNestedPropertyName("monitoringSatellites");
        satelliteParam.setNestedEntityTypeLabel("Satellite"); //Collision is an RE
        satelliteParam.setNestedRelationshipEntity(true);
        satelliteParam.setRelationshipType("MONITORED_BY"); //assume COLLIDES is the RE type
        satelliteParam.setRelationshipDirection(Relationship.Direction.INCOMING);
        satelliteParam.setNestedRelationshipEntity(true);

        assertThat(queryStatements.findByType("Asteroid", new Filters().add(planetParam).add(satelliteParam), 1)
            .getStatement()).isEqualTo(
            "MATCH (n:`Asteroid`) MATCH (n)-[r0:`COLLIDES`]->(m0) WHERE r0.`totalDestructionProbability` = $`collision_totalDestructionProbability_0` "
                +
                "MATCH (n)<-[r1:`MONITORED_BY`]-(m1) WHERE r1.`signalStrength` >= $`monitoringSatellites_signalStrength_1` WITH DISTINCT n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-632
    public void testFindByNestedBaseAndREProperty() {
        Filter planetParam = new Filter("totalDestructionProbability", ComparisonOperator.EQUALS, "20");
        planetParam.setNestedPropertyName("collision");
        planetParam.setNestedEntityTypeLabel("Collision"); //Collision is an RE
        planetParam.setNestedRelationshipEntity(true);
        planetParam.setRelationshipType("COLLIDES"); //assume COLLIDES is the RE type
        planetParam.setRelationshipDirection(Relationship.Direction.OUTGOING);
        planetParam.setNestedRelationshipEntity(true);

        Filter moonParam = orbitsMoonFilter();
        moonParam.setBooleanOperator(BooleanOperator.AND);

        assertThat(queryStatements.findByType("Asteroid", new Filters().add(planetParam, moonParam), 1).getStatement())
            .isEqualTo("MATCH (n:`Asteroid`) MATCH (n)-[r0:`COLLIDES`]->(m0) " +
                "WHERE r0.`totalDestructionProbability` = $`collision_totalDestructionProbability_0` " +
                "MATCH (m1:`Moon`) WHERE m1.`name` = $`moon_name_1` MATCH (n)<-[:`ORBITS`]-(m1) WITH DISTINCT n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    @Test // DATAGRAPH-629
    public void testFindByDifferentNestedPropertiesAnded() {
        Filter planetParam = collidesWithEarthFilter();

        Filter moonParam = orbitsMoonFilter();
        moonParam.setBooleanOperator(BooleanOperator.AND);
        assertThat(queryStatements.findByType("Asteroid",
            new Filters().add(planetParam).add(moonParam), 1).getStatement())
            .isEqualTo("MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = $`collidesWith_name_0` " +
                "MATCH (m1:`Moon`) WHERE m1.`name` = $`moon_name_1` MATCH (n)-[:`COLLIDES`]->(m0) " +
                "MATCH (n)<-[:`ORBITS`]-(m1) WITH DISTINCT n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    // Would be something like findByMoonSomethingAndFindByPlanetXOrFindByPlanetY
    @Test
    public void testFindByNestedPropertiesOnTheSameEntityOredNotFollowingEachOtherDirectly() {
        Filter planetParam = collidesWithEarthFilter();

        Filter planetParam2 = collidesWithMarsFilter();
        planetParam2.setBooleanOperator(BooleanOperator.OR);

        Filter moonParam = orbitsMoonFilter();
        moonParam.setBooleanOperator(BooleanOperator.AND);
        assertThat(queryStatements.findByType("Asteroid",
            new Filters().add(planetParam).add(moonParam).add(planetParam2), 1).getStatement())
            .isEqualTo(
                "MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = $`collidesWith_name_0` OR m0.`name` = $`collidesWith_name_2` "
                    +
                    "MATCH (m1:`Moon`) WHERE m1.`name` = $`moon_name_1` MATCH (n)-[:`COLLIDES`]->(m0) " +
                    "MATCH (n)<-[:`ORBITS`]-(m1) WITH DISTINCT n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    // Would be something like findByMoonSomethingAndFindByPlanetXOrFindByPlanetY
    @Test
    public void testFindByNestedPropertiesOnTheSameEntityOred() {
        Filter planetParam = collidesWithEarthFilter();
        planetParam.setBooleanOperator(BooleanOperator.AND);

        Filter planetParam2 = collidesWithMarsFilter();
        planetParam2.setBooleanOperator(BooleanOperator.OR);

        Filter moonParam = orbitsMoonFilter();

        assertThat(queryStatements.findByType("Asteroid",
            new Filters().add(moonParam).add(planetParam).add(planetParam2), 1).getStatement())
            .isEqualTo("MATCH (n:`Asteroid`) MATCH (m0:`Moon`) WHERE m0.`name` = $`moon_name_0` " +
                "MATCH (m1:`Planet`) WHERE m1.`name` = $`collidesWith_name_1` OR m1.`name` = $`collidesWith_name_2` " +
                "MATCH (n)<-[:`ORBITS`]-(m0) MATCH (n)-[:`COLLIDES`]->(m1) WITH DISTINCT n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    // Would be something like findByMoonSomethingORFindByPlanetXOrFindByPlanetY
    @Test(expected = UnsupportedOperationException.class)
    public void testFindByNestedPropertiesOnTheSameEntityOredWithAnotherFilter() {
        Filter planetParam = collidesWithEarthFilter();
        planetParam.setBooleanOperator(BooleanOperator.OR);

        Filter planetParam2 = collidesWithMarsFilter();
        planetParam2.setBooleanOperator(BooleanOperator.OR);

        Filter moonParam = orbitsMoonFilter();

        queryStatements.findByType("Asteroid",
            new Filters().add(moonParam).add(planetParam).add(planetParam2), 1).getStatement();
    }

    // Would be something like findByMoonSomethingORFindByPlanetXOrFindByPlanetY
    @Test(expected = UnsupportedOperationException.class)
    public void testFindByNestedPropertiesOnTheSameEntityOredNotFollowingEachOtherDirectlyWithAnotherFilter() {
        Filter planetParam = collidesWithEarthFilter();

        Filter planetParam2 = collidesWithMarsFilter();
        planetParam2.setBooleanOperator(BooleanOperator.OR);

        Filter moonParam = orbitsMoonFilter();
        moonParam.setBooleanOperator(BooleanOperator.OR);
        queryStatements.findByType("Asteroid",
            new Filters().add(planetParam).add(moonParam).add(planetParam2), 1).getStatement();
    }

    @Test(expected = UnsupportedOperationException.class) // DATAGRAPH-662
    public void testFindByDifferentNestedPropertiesOred() {
        Filter planetParam = collidesWithEarthFilter();

        Filter moonParam = orbitsMoonFilter();
        moonParam.setBooleanOperator(BooleanOperator.OR);

        queryStatements.findByType("Asteroid", new Filters().add(planetParam).add(moonParam), 1).getStatement();
    }

    @Test // DATAGRAPH-629
    public void testFindByMultipleNestedPropertiesAnded() {
        Filter planetParam = collidesWithEarthFilter();

        Filter moonParam = new Filter("size", ComparisonOperator.EQUALS, "5");
        moonParam.setNestedPropertyName("collidesWith");
        moonParam.setNestedEntityTypeLabel("Planet");
        moonParam.setRelationshipType("COLLIDES");
        moonParam.setRelationshipDirection(Relationship.Direction.OUTGOING);
        moonParam.setBooleanOperator(BooleanOperator.AND);
        assertThat(
            queryStatements.findByType("Asteroid", new Filters().add(planetParam).add(moonParam), 1).getStatement())
            .isEqualTo(
                "MATCH (n:`Asteroid`) MATCH (m0:`Planet`) WHERE m0.`name` = $`collidesWith_name_0` AND m0.`size` = $`collidesWith_size_1` MATCH (n)-[:`COLLIDES`]->(m0) WITH DISTINCT n MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    @Test(expected = MissingOperatorException.class) // GH-73
    public void testFindByMultipleAndPropertiesWithMissingBooleanOperator() {
        Filter nameParam = new Filter("name", ComparisonOperator.EQUALS, "AST-1");
        Filter diameterParam = new Filter("diameter", ComparisonOperator.LESS_THAN, 60);
        queryStatements.findByType("Asteroid", new Filters().add(nameParam).add(diameterParam), 2).getStatement();
    }

    private static Filter collidesWithEarthFilter() {
        Filter planetParam = new Filter("name", ComparisonOperator.EQUALS, "Earth");
        planetParam.setNestedPropertyName("collidesWith");
        planetParam.setNestedEntityTypeLabel("Planet");
        planetParam.setRelationshipType("COLLIDES");
        planetParam.setRelationshipDirection(Relationship.Direction.OUTGOING);
        return planetParam;
    }

    private static Filter orbitsMoonFilter() {
        Filter moonParam = new Filter("name", ComparisonOperator.EQUALS, "Moon");
        moonParam.setNestedPropertyName("moon");
        moonParam.setNestedEntityTypeLabel("Moon");
        moonParam.setRelationshipType("ORBITS");
        moonParam.setRelationshipDirection(Relationship.Direction.INCOMING);
        return moonParam;
    }

    private static Filter collidesWithMarsFilter() {
        Filter planetParam2 = new Filter("name", ComparisonOperator.EQUALS, "Mars");
        planetParam2.setNestedPropertyName("collidesWith");
        planetParam2.setNestedEntityTypeLabel("Planet");
        planetParam2.setRelationshipType("COLLIDES");
        planetParam2.setRelationshipDirection(Relationship.Direction.OUTGOING);
        return planetParam2;
    }
}
