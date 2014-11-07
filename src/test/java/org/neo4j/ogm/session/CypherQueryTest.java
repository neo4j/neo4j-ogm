package org.neo4j.ogm.session;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CypherQueryTest {

    @Test
    public void testFindOne() throws Exception {
        assertEquals("MATCH p=(n)-->(m) WHERE id(n) = 123 RETURN p", new CypherQuery().findOne(123L));
    }

    @Test
    public void testFindAll() throws Exception {
        List<Long> ids = Arrays.asList(new Long[] { 123L, 234L, 345L });
        assertEquals("MATCH p=(n)-->(m) WHERE id(n) in [123,234,345] RETURN p", new CypherQuery().findAll(ids));
    }

    @Test
    public void testFindByLabel() throws Exception {
        List<String> labels = Arrays.asList(new String[] { "NODE", "VERTEX" });
        assertEquals("MATCH p=(n:NODE:VERTEX)-->(m) RETURN p", new CypherQuery().findByLabel(labels));
    }

    @Test
    public void findAll() throws Exception {
        assertEquals("MATCH p=()-->() RETURN p", new CypherQuery().findAll());
    }

    @Test
    public void delete() throws Exception {
        assertEquals("MATCH (n) WHERE id(n) = 123 OPTIONAL MATCH (n)-[r]-() DELETE r, n", new CypherQuery().delete(123L));
    }

    @Test
    public void deleteAll() throws Exception {
        List<Long> ids = Arrays.asList(new Long[] { 123L, 234L, 345L });
        assertEquals("MATCH (n) WHERE id(n) in [123,234,345] OPTIONAL MATCH (n)-[r]-() DELETE r, n", new CypherQuery().deleteAll(ids));
    }

    @Test
    public void deleteAllByLabel() throws Exception {
        List<String> labels = Arrays.asList(new String[] { "NODE", "VERTEX" });
        assertEquals("MATCH (n:NODE:VERTEX) OPTIONAL MATCH (n)-[r]-() DELETE r, n", new CypherQuery().deleteByLabel(labels));
    }

    @Test
    public void purge() throws Exception {
        assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n", new CypherQuery().purge());
    }
}
