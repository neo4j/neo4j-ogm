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

import java.util.Arrays;

import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class NodeEntityQueryPagingTest {

    private final NodeQueryStatements<Long> queryStatements = new NodeQueryStatements<>();
    private Pagination paging = new Pagination(2, 2);
    private Filters filters = new Filters().add(new Filter("name", ComparisonOperator.EQUALS, "velociraptor"));

    @Test
    public void testFindByType() {
        assertThat(queryStatements.findByType("Raptor", 1).setPagination(paging).getStatement())
            .isEqualTo("MATCH (n:`Raptor`) WITH n SKIP 4 LIMIT 2 MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    @Test
    public void testFindByTypeZeroDepth() throws Exception {
        assertThat(queryStatements.findByType("Raptor", 0).setPagination(paging).getStatement())
            .isEqualTo("MATCH (n:`Raptor`) WITH n SKIP 4 LIMIT 2 RETURN n");
    }

    @Test
    public void testFindByTypeInfiniteDepth() throws Exception {
        assertThat(queryStatements.findByType("Raptor", -1).setPagination(paging).getStatement())
            .isEqualTo("MATCH (n:`Raptor`) WITH n SKIP 4 LIMIT 2 MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)");
    }

    @Test
    public void testFindByProperty() {
        assertThat(queryStatements.findByType("Raptor", filters, 2).setPagination(paging).getStatement())
            .isEqualTo(
                "MATCH (n:`Raptor`) WHERE n.`name` = $`name_0` WITH n SKIP 4 LIMIT 2 MATCH p=(n)-[*0..2]-(m) RETURN p, ID(n)");
    }

    @Test
    public void testFindByPropertyZeroDepth() {
        assertThat(queryStatements.findByType("Raptor", filters, 0).setPagination(paging).getStatement())
            .isEqualTo("MATCH (n:`Raptor`) WHERE n.`name` = $`name_0` WITH n SKIP 4 LIMIT 2 RETURN n");
    }

    @Test
    public void testFindByPropertyInfiniteDepth() {
        assertThat(queryStatements.findByType("Raptor", filters, -1).setPagination(paging).getStatement())
            .isEqualTo(
                "MATCH (n:`Raptor`) WHERE n.`name` = $`name_0` WITH n SKIP 4 LIMIT 2 MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)");
    }

    @Test
    public void testFindAllByType() throws Exception {
        assertThat(
            queryStatements.findAllByType("Raptor", Arrays.asList(1L, 2L), 1).setPagination(paging).getStatement())
            .isEqualTo(
                "MATCH (n:`Raptor`) WHERE ID(n) IN $ids WITH n SKIP 4 LIMIT 2 MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

    @Test
    public void testFindAllByTypeZeroDepth() throws Exception {
        assertThat(
            queryStatements.findAllByType("Raptor", Arrays.asList(1L, 2L), 0).setPagination(paging).getStatement())
            .isEqualTo("MATCH (n:`Raptor`) WHERE ID(n) IN $ids WITH n SKIP 4 LIMIT 2 RETURN n");
    }

    @Test
    public void testFindAllByTypeInfiniteDepth() throws Exception {
        assertThat(
            queryStatements.findAllByType("Raptor", Arrays.asList(1L, 2L), -1).setPagination(paging).getStatement())
            .isEqualTo(
                "MATCH (n:`Raptor`) WHERE ID(n) IN $ids WITH n SKIP 4 LIMIT 2 MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)");
    }

    @Test
    public void testFindByTypeAndOffset() {
        Pagination pagination = new Pagination(1, 5);
        pagination.setOffset(3);
        PagingAndSortingQuery query = queryStatements.findByType("Raptor", 1).setPagination(pagination);
        assertThat(query.getStatement())
            .isEqualTo("MATCH (n:`Raptor`) WITH n SKIP 3 LIMIT 5 MATCH p=(n)-[*0..1]-(m) RETURN p, ID(n)");
    }

}
