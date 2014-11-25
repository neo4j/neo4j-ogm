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

        assertEquals("{\"statements\":[{\"statement\":\"MATCH p=(n)-[*1..1]-(m) WHERE id(n) = { id } RETURN collect(distinct p)\",\"parameters\":{\"id\":123},\"resultDataContents\":[\"graph\"]}]}", cypher);

    }

}
