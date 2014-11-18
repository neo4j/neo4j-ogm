package org.neo4j.ogm.unit.mapper.cypher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.neo4j.ogm.mapper.cypher.GraphModelQuery;
import org.neo4j.ogm.mapper.cypher.ParameterisedStatement;
import org.neo4j.ogm.mapper.cypher.ParameterisedStatements;
import org.neo4j.ogm.session.querystrategy.DepthOneStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParameterisedStatementsTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testStatement() throws Exception {

        String query = new DepthOneStrategy().findOne(123L);
        List<ParameterisedStatement> statements = new ArrayList<>();

        statements.add(new GraphModelQuery(query, new HashMap<String, Object>()));

        String cypher = mapper.writeValueAsString(new ParameterisedStatements(statements));

        assertEquals("{\"statements\":[{\"statement\":\"MATCH p=(n)--(m) WHERE id(n) = 123 RETURN p\",\"parameters\":{},\"resultDataContents\":[\"graph\"]}]}", cypher);

    }

}
