package org.neo4j.ogm.unit.mapper.cypher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.neo4j.ogm.mapper.cypher.ParameterisedStatement;
import org.neo4j.ogm.mapper.cypher.ParameterisedStatements;
import org.neo4j.ogm.session.querystrategy.DepthOneStrategy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParameterisedStatementsTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testStatement() throws Exception {

        List<ParameterisedStatement> statements = new ArrayList<>();
        statements.add(new DepthOneStrategy().findOne(123L));

        String cypher = mapper.writeValueAsString(new ParameterisedStatements(statements));

        assertEquals("{\"statements\":[{\"statement\":\"MATCH p=(n)--(m) WHERE id(n) = { id } RETURN p\",\"parameters\":{\"id\":123},\"resultDataContents\":[\"graph\"]}]}", cypher);

    }

}
