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
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;

/**
 * @author Vince Bickers
 * @author Jasper Blues
 * @author Michael J. Simons
 */
public class RelationshipDeleteStatementsTest {

    private final DeleteStatements statements = new RelationshipDeleteStatements();

    @Test
    public void testDeleteOne() {
        assertThat(statements.delete(0L).getStatement())
            .isEqualTo("MATCH (n)-[r0]->() WHERE ID(r0) = $id DELETE r0");
    }

    @Test
    public void testDeleteMany() {
        assertThat(statements.delete(Arrays.asList(1L, 2L)).getStatement())
            .isEqualTo("MATCH (n)-[r0]->() WHERE ID(r0) IN $ids DELETE r0");
    }

    @Test
    public void testDeleteAll() {
        assertThat(statements.deleteAll().getStatement()).isEqualTo("MATCH (n) OPTIONAL MATCH (n)-[r0]-() DELETE r0");
    }

    @Test
    public void testDeleteWithType() {
        assertThat(statements.delete("TRAFFIC_WARDEN").getStatement())
            .isEqualTo("MATCH (n)-[r0:`TRAFFIC_WARDEN`]-() DELETE r0");
    }

    @Test
    public void testDeleteWithTypeAndFilters() {
        CypherQuery query = statements
            .delete("INFLUENCE", new Filters().add(new Filter("score", ComparisonOperator.EQUALS, -12.2)));
        assertThat(query.getStatement())
            .isEqualTo("MATCH (n)-[r0:`INFLUENCE`]->(m) WHERE r0.`score` = $`score_0`  DELETE r0");
    }

    @Test
    public void testDeleteWithTypeAndFiltersAndList() {
        CypherQuery query = statements
            .deleteAndList("INFLUENCE", new Filters().add(new Filter("score", ComparisonOperator.EQUALS, -12.2)));
        assertThat(query.getStatement())
            .isEqualTo("MATCH (n)-[r0:`INFLUENCE`]->(m) WHERE r0.`score` = $`score_0`  DELETE r0 RETURN ID(r0)");
    }
}
