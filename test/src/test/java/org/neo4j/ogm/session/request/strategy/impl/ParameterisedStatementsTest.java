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

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.neo4j.ogm.config.ObjectMapperFactory;
import org.neo4j.ogm.cypher.query.DefaultGraphModelRequest;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.Statements;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Vince Bickers
 */
public class ParameterisedStatementsTest {

    private static final ObjectMapper mapper = ObjectMapperFactory.objectMapper();

    @Test
    public void testStatement() throws Exception {

        List<Statement> statements = new ArrayList<>();
        PagingAndSortingQuery query = new NodeQueryStatements().findOne(123L, 1);
        statements.add(new DefaultGraphModelRequest(query.getStatement(), query.getParameters()));

        String cypher = mapper.writeValueAsString(new Statements(statements));

        assertThat(cypher).isEqualTo(
            "{\"statements\":[{\"statement\":\"MATCH (n) WHERE ID(n) = { id } WITH n MATCH p=(n)-[*0..1]-(m) RETURN p\",\"parameters\":{\"id\":123},\"resultDataContents\":[\"graph\"],\"includeStats\":false}]}");
    }
}
