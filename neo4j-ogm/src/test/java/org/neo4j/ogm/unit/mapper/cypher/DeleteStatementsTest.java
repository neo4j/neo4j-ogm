package org.neo4j.ogm.unit.mapper.cypher;

import org.junit.Test;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DeleteStatementsTest {

    private final DeleteStatements statements = new DeleteStatements();

    @Test
    public void testDeleteOne() {
        assertEquals("MATCH (n) WHERE id(n) = { id } OPTIONAL MATCH (n)-[r]-() DELETE r, n", statements.delete(0L).getStatement());
    }

    @Test
    public void testDeleteAll() {
        assertEquals("MATCH (n) WHERE id(n) in { ids } OPTIONAL MATCH (n)-[r]-() DELETE r, n", statements.deleteAll(Arrays.asList(1L, 2L)).getStatement());
    }

    @Test
    public void testPurge() {
        assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n", statements.purge().getStatement());
    }

    @Test
    public void testDeleteByLabel() {
        assertEquals("MATCH (n:TRAFFIC_WARDENS) OPTIONAL MATCH (n)-[r]-() DELETE r, n", statements.deleteByLabel("TRAFFIC_WARDENS").getStatement());
    }
}
