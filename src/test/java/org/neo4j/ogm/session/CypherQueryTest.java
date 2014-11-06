package org.neo4j.ogm.session;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CypherQueryTest {

    @Test
    public void testFindOne() throws Exception {
        assertEquals("MATCH p=(n)-->(m) WHERE id(n) = 123 RETURN p;", new CypherQuery().findOne(123L));
    }

    @Test
    public void testFindAll() throws Exception {
        List<Long> ids = Arrays.asList(new Long[] { 123L, 234L, 345L });
        assertEquals("MATCH p=(n)-->(m) WHERE id(n) in [123,234,345] RETURN p;", new CypherQuery().findAll(ids));
    }

    @Test
    public void testFindByLabel() throws Exception {
        List<String> labels = Arrays.asList(new String[] { "NODE", "VERTEX" });
        assertEquals("MATCH p=(n:NODE:VERTEX)-->(m) RETURN p;", new CypherQuery().findByLabel(labels));
    }
}
