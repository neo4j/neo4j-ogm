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

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;

import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.session.request.strategy.AggregateStatements;

/**
 * @author Vince Bickers
 * @author Jasper Blues
 * @author Michael J. Simons
 */
public class CountStatementsTest {

    private AggregateStatements statements = new CountStatements();

    @Test
    public void testCountNodesWithLabel() throws Exception {
        assertThat(statements.countNodes(singleton("Person")).getStatement())
            .isEqualTo("MATCH (n:`Person`) RETURN COUNT(n)");
    }

    @Test
    public void testCountNodesWithMultipleLabels() throws Exception {
        assertThat(statements.countNodes(Arrays.asList("Person", "Candidate")).getStatement())
            .isEqualTo("MATCH (n:`Person`:`Candidate`) RETURN COUNT(n)");
    }

    @Test
    public void testCountNodesWithLabelAndFilters() throws Exception {
        CypherQuery query = statements
            .countNodes("Person", new Filters().add(new Filter("name", ComparisonOperator.EQUALS, "Jim")));
        assertThat(query.getStatement())
            .isEqualTo("MATCH (n:`Person`) WHERE n.`name` = $`name_0` WITH n RETURN COUNT(n)");
        assertThat(query.getParameters().toString()).isEqualTo("{name_0=Jim}");
    }

    @Test
    public void testCountEdgesWithTypeAndFilters() throws Exception {
        CypherQuery query = statements
            .countEdges("INFLUENCE", new Filters().add(new Filter("score", ComparisonOperator.EQUALS, -12.2)));
        assertThat(query.getStatement())
            .isEqualTo("MATCH (n)-[r0:`INFLUENCE`]->(m) WHERE r0.`score` = $`score_0`  RETURN COUNT(r0)");
        assertThat(query.getParameters().toString()).isEqualTo("{score_0=-12.2}");
    }

    @Test
    public void testCountEdgesWithSpecificPath() throws Exception {
        assertThat(statements.countEdges("StartNode", "TYPE", "EndNode").getStatement())
            .isEqualTo("MATCH (:`StartNode`)-[r0:`TYPE`]->(:`EndNode`) RETURN COUNT(r0)");
    }
}
