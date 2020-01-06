/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;

/**
 * @author vince
 */
public class NodeDeleteStatementsTest {

    private final DeleteStatements statements = new NodeDeleteStatements();

    @Test
    public void testDeleteOne() throws Exception {
        assertThat(statements.delete(0L).getStatement())
            .isEqualTo("MATCH (n) WHERE ID(n) = { id } OPTIONAL MATCH (n)-[r0]-() DELETE r0, n");
    }

    @Test
    public void testDeleteMany() throws Exception {
        assertThat(statements.delete(Arrays.asList(1L, 2L)).getStatement())
            .isEqualTo("MATCH (n) WHERE ID(n) in { ids } OPTIONAL MATCH (n)-[r0]-() DELETE r0, n");
    }

    @Test
    public void testDeleteAll() throws Exception {
        assertThat(statements.deleteAll().getStatement())
            .isEqualTo("MATCH (n) OPTIONAL MATCH (n)-[r0]-() DELETE r0, n");
    }

    @Test
    public void testDeleteWithLabel() throws Exception {
        assertThat(statements.delete("TRAFFIC_WARDENS").getStatement())
            .isEqualTo("MATCH (n:`TRAFFIC_WARDENS`) OPTIONAL MATCH (n)-[r0]-() DELETE r0, n");
    }

    @Test
    public void testDeleteWithLabelAndFilters() throws Exception {
        CypherQuery query = statements
            .delete("INFLUENCE", new Filters().add(new Filter("score", ComparisonOperator.EQUALS, -12.2)));
        assertThat(query.getStatement()).isEqualTo(
            "MATCH (n:`INFLUENCE`) "
                + "WHERE n.`score` = { `score_0` } "
                + "WITH n "
                + "OPTIONAL MATCH (n)-[r0]-() "
                + "DELETE r0, n");
    }

    @Test
    public void testDeleteWithLabelAndFiltersAndList() throws Exception {
        CypherQuery query = statements
            .deleteAndList("INFLUENCE", new Filters().add(new Filter("score", ComparisonOperator.EQUALS, -12.2)));
        assertThat(query.getStatement()).isEqualTo(
            "MATCH (n:`INFLUENCE`) "
                + "WHERE n.`score` = { `score_0` } "
                + "WITH n "
                + "OPTIONAL MATCH (n)-[r0]-() "
                + "DELETE r0, n RETURN ID(n)");
    }
}
