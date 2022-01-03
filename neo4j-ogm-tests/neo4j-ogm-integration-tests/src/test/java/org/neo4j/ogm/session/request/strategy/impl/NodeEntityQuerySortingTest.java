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

import static org.assertj.core.api.Assertions.*;
import static org.neo4j.ogm.cypher.ComparisonOperator.*;
import static org.neo4j.ogm.cypher.query.SortOrder.Direction.*;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.SortOrder;

/**
 * @author Vince Bickers
 * @author Jonathan D'Orleans
 */
public class NodeEntityQuerySortingTest {

    private final NodeQueryStatements query = new NodeQueryStatements();

    private SortOrder sortOrder;
    private Filters filters;

    @Before
    public void setUp() {
        sortOrder = new SortOrder();
        filters = new Filters();
    }

    @Test
    public void testFindByType() {
        sortOrder.add("name");
        check("MATCH (n:`Raptor`) WITH n ORDER BY n.name MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)",
            query.findByType("Raptor", 1).setSortOrder(sortOrder).getStatement());
    }

    @Test
    public void testFindByProperty() {
        sortOrder.add(DESC, "weight");
        filters.add(new Filter("name", EQUALS, "velociraptor"));
        check(
            "MATCH (n:`Raptor`) WHERE n.`name` = $`name_0` WITH n ORDER BY n.weight DESC MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)",
            query.findByType("Raptor", filters, 2).setSortOrder(sortOrder).getStatement());
    }

    @Test
    public void testFindByTypeDepthZero() {
        sortOrder.add(DESC, "name");
        check("MATCH (n:`Raptor`) WITH n ORDER BY n.name DESC RETURN n",
            query.findByType("Raptor", 0).setSortOrder(sortOrder).getStatement());
    }

    @Test
    public void testByPropertyDepthZero() {
        filters.add(new Filter("name", EQUALS, "velociraptor"));
        sortOrder.add(DESC, "weight");
        check("MATCH (n:`Raptor`) WHERE n.`name` = $`name_0` WITH n ORDER BY n.weight DESC RETURN n",
            query.findByType("Raptor", filters, 0).setSortOrder(sortOrder).getStatement());
    }

    @Test
    public void testFindByTypeDepthInfinite() {
        sortOrder.add(DESC, "name");
        check("MATCH (n:`Raptor`) WITH n ORDER BY n.name DESC MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)",
            query.findByType("Raptor", -1).setSortOrder(sortOrder).getStatement());
    }

    @Test
    public void testFindByPropertyDepthInfinite() {
        sortOrder.add(DESC, "name");
        filters.add(new Filter("name", EQUALS, "velociraptor"));
        check(
            "MATCH (n:`Raptor`) WHERE n.`name` = $`name_0` WITH n ORDER BY n.name DESC MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)",
            query.findByType("Raptor", filters, -1).setSortOrder(sortOrder).getStatement());
    }

    @Test
    public void testMultipleSortOrders() {
        String cypher = "MATCH (n:`Raptor`) WITH n ORDER BY n.age DESC,n.name DESC RETURN n";
        check(cypher, query.findByType("Raptor", 0).setSortOrder(sortOrder.add(DESC, "age", "name")).getStatement());
        check(cypher, query.findByType("Raptor", 0).setSortOrder(new SortOrder(DESC, "age", "name")).getStatement());
        check(cypher, query.findByType("Raptor", 0).setSortOrder(new SortOrder().desc("age", "name")).getStatement());
    }

    @Test
    public void testDefaultMultipleSortOrders() {
        String cypher = "MATCH (n:`Raptor`) WITH n ORDER BY n.age,n.name RETURN n";
        check(cypher, query.findByType("Raptor", 0).setSortOrder(sortOrder.add("age", "name")).getStatement());
        check(cypher, query.findByType("Raptor", 0).setSortOrder(new SortOrder("age", "name")).getStatement());
        check(cypher, query.findByType("Raptor", 0).setSortOrder(new SortOrder().asc("age", "name")).getStatement());
    }

    @Test
    public void testDifferentSortDirections() {
        sortOrder.add(DESC, "age").add("name");
        check("MATCH (n:`Raptor`) WITH n ORDER BY n.age DESC,n.name RETURN n",
            query.findByType("Raptor", 0).setSortOrder(sortOrder).getStatement());
    }

    private void check(String expected, String actual) {
        assertThat(actual).isEqualTo(expected);
    }
}
