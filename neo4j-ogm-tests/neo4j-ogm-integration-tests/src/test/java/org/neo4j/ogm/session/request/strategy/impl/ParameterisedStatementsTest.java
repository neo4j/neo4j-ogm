/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
 * @author Michael J. Simons
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
            "{\"statements\":[{\"statement\":\"MATCH (n) WHERE ID(n) = $id WITH n MATCH p=(n)-[*0..1]-(m) RETURN p\",\"parameters\":{\"id\":123},\"resultDataContents\":[\"graph\"],\"includeStats\":false}]}");
    }
}
