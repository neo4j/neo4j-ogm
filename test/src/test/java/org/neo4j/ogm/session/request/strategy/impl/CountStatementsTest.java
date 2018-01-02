/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

import static com.google.common.collect.Lists.*;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.session.request.strategy.AggregateStatements;

/**
 * @author Vince Bickers
 * @author Jasper Blues
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
        assertThat(statements.countNodes(newArrayList("Person", "Candidate")).getStatement())
            .isEqualTo("MATCH (n:`Person`:`Candidate`) RETURN COUNT(n)");
    }

    @Test
    public void testCountNodesWithLabelAndFilters() throws Exception {
        CypherQuery query = statements
            .countNodes("Person", new Filters().add(new Filter("name", ComparisonOperator.EQUALS, "Jim")));
        assertThat(query.getStatement())
            .isEqualTo("MATCH (n:`Person`) WHERE n.`name` = { `name_0` } WITH n RETURN COUNT(n)");
        assertThat(query.getParameters().toString()).isEqualTo("{name_0=Jim}");
    }

    @Test
    public void testCountEdgesWithTypeAndFilters() throws Exception {
        CypherQuery query = statements
            .countEdges("INFLUENCE", new Filters().add(new Filter("score", ComparisonOperator.EQUALS, -12.2)));
        assertThat(query.getStatement())
            .isEqualTo("MATCH (n)-[r0:`INFLUENCE`]->(m) WHERE r0.`score` = { `score_0` }  RETURN COUNT(r0)");
        assertThat(query.getParameters().toString()).isEqualTo("{score_0=-12.2}");
    }

    @Test
    public void testCountEdgesWithSpecificPath() throws Exception {
        assertThat(statements.countEdges("StartNode", "TYPE", "EndNode").getStatement())
            .isEqualTo("MATCH (:`StartNode`)-[r0:`TYPE`]->(:`EndNode`) RETURN COUNT(r0)");
    }
}
