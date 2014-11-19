package org.neo4j.ogm.unit.mapper.cypher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.graphmodel.Property;
import org.junit.Test;
import org.neo4j.ogm.mapper.cypher.ParameterisedStatement;
import org.neo4j.ogm.session.strategy.DepthOneStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CypherQueryTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private ParameterisedStatement statement;

    @Test
    public void testFindOne() throws Exception {
        statement = new DepthOneStrategy().findOne(123L);
        assertEquals("MATCH p=(n)--(m) WHERE id(n) = { id } RETURN p", statement.getStatement());
        assertEquals("{\"id\":123}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void testFindAll() throws Exception {
        List<Long> ids = Arrays.asList(new Long[] { 123L, 234L, 345L });
        statement = new DepthOneStrategy().findAll(ids);
        assertEquals("MATCH p=(n)--(m) WHERE id(n) in { ids } RETURN p", statement.getStatement());
        assertEquals("{\"ids\":[123,234,345]}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void testFindByLabel() throws Exception {
        statement = new DepthOneStrategy().findByLabel("NODE");
        assertEquals("MATCH p=(n:NODE)--(m) RETURN p", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findAll() throws Exception {
        statement = new DepthOneStrategy().findAll();
        assertEquals("MATCH p=()-->() RETURN p", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyStringValue() throws Exception {
        statement = new DepthOneStrategy().findByProperty("Asteroid", new Property<String, Object>("ref", "45 Eugenia"));
        assertEquals("MATCH p=(n:Asteroid { ref } )--(m) return p", statement.getStatement());
        assertEquals("{\"ref\":\"45 Eugenia\"}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyIntegralValue() throws Exception {
        statement =  new DepthOneStrategy().findByProperty("Asteroid", new Property<String, Object>("index", 77));
        assertEquals("MATCH p=(n:Asteroid { index } )--(m) return p",statement.getStatement());
        assertEquals("{\"index\":77}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyStandardForm() throws Exception {
        statement = new DepthOneStrategy().findByProperty("Asteroid", new Property<String, Object>("diameter", 6.02E1));
        assertEquals("MATCH p=(n:Asteroid { diameter } )--(m) return p", statement.getStatement());
        assertEquals("{\"diameter\":60.2}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyDecimal() throws Exception {
        statement = new DepthOneStrategy().findByProperty("Asteroid", new Property<String, Object>("diameter", 60.2));
        assertEquals("MATCH p=(n:Asteroid { diameter } )--(m) return p", statement.getStatement());
        assertEquals("{\"diameter\":60.2}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyEmbeddedDelimiter() throws Exception {
        statement = new DepthOneStrategy().findByProperty("Cookbooks", new Property<String, Object>("title", "Mrs Beeton's Household Recipes"));
        assertEquals("MATCH p=(n:Cookbooks { title } )--(m) return p", statement.getStatement());
        assertEquals("{\"title\":\"Mrs Beeton's Household Recipes\"}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void delete() throws Exception {
        statement = new DepthOneStrategy().delete(123L);
        assertEquals("MATCH (n) WHERE id(n) = { id } OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{\"id\":123}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void deleteAll() throws Exception {
        List<Long> ids = Arrays.asList(new Long[] { 123L, 234L, 345L });
        statement = new DepthOneStrategy().deleteAll(ids);
        assertEquals("MATCH (n) WHERE id(n) in { ids } OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{\"ids\":[123,234,345]}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void deleteAllByLabel() throws Exception {
        statement = new DepthOneStrategy().deleteByLabel("NODE");
        assertEquals("MATCH (n:NODE) OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void purge() throws Exception {
        statement = new DepthOneStrategy().purge();
        assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void testUpdateProperties() throws Exception {
        List<Property<String, Object>> properties = new ArrayList<>();
        properties.add(new Property<String, Object>("iProp", 42));
        properties.add(new Property<String, Object>("fProp", 3.1415928));
        properties.add(new Property<String, Object>("sProp", "Pie and the meaning of life"));

        statement = new DepthOneStrategy().updateProperties(123L, properties);
        assertEquals("MATCH (n) WHERE id(n) = { id } SET n.iProp=42,n.fProp=3.1415928,n.sProp=\\\"Pie and the meaning of life\\\"", statement.getStatement());
        assertEquals("{\"id\":123}", mapper.writeValueAsString(statement.getParameters()));
    }


    @Test
    public void testCreateNode() throws Exception {
        List<Property<String, Object>> properties = new ArrayList<>();
        properties.add(new Property<String, Object>("iProp", 42));
        properties.add(new Property<String, Object>("fProp", 3.1415928));
        properties.add(new Property<String, Object>("sProp", "Pie and the meaning of life"));

        List<String> labels = new ArrayList<>();
        labels.add("NODE");
        labels.add("VERTEX");

        statement = new DepthOneStrategy().createNode(properties, labels);
        assertEquals("CREATE (n:NODE:VERTEX { properties }) return id(n)", statement.getStatement());
        assertEquals("{\"properties\":{\"sProp\":\"Pie and the meaning of life\",\"fProp\":3.1415928,\"iProp\":42}}", mapper.writeValueAsString(statement.getParameters()));
    }
}
