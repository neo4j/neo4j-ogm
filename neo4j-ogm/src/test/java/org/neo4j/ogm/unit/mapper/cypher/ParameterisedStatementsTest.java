/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.unit.mapper.cypher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.cypher.statement.ParameterisedStatements;
import org.neo4j.ogm.session.request.strategy.VariableDepthQuery;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParameterisedStatementsTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testStatement() throws Exception {

        List<ParameterisedStatement> statements = new ArrayList<>();
        statements.add(new VariableDepthQuery().findOne(123L, 1));

        String cypher = mapper.writeValueAsString(new ParameterisedStatements(statements));

        assertEquals("{\"statements\":[{\"statement\":\"MATCH p=(n)-[*0..1]-(m) WHERE id(n) = { id } RETURN collect(distinct p)\",\"parameters\":{\"id\":123},\"resultDataContents\":[\"graph\"]}]}", cypher);

    }

}
