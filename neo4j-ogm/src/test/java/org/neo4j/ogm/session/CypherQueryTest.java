package org.neo4j.ogm.session;

import org.graphaware.graphmodel.neo4j.Property;
import org.junit.Test;
import org.neo4j.ogm.session.querystrategy.DepthOneStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CypherQueryTest {

    @Test
    public void testFindOne() throws Exception {
        assertEquals("MATCH p=(n)--(m) WHERE id(n) = 123 RETURN p", new DepthOneStrategy().findOne(123L));
    }

    @Test
    public void testFindAll() throws Exception {
        List<Long> ids = Arrays.asList(new Long[] { 123L, 234L, 345L });
        assertEquals("MATCH p=(n)--(m) WHERE id(n) in [123,234,345] RETURN p", new DepthOneStrategy().findAll(ids));
    }

    @Test
    public void testFindByLabel() throws Exception {
        assertEquals("MATCH p=(n:NODE)--(m) RETURN p", new DepthOneStrategy().findByLabel("NODE"));
    }

    @Test
    public void findAll() throws Exception {
        assertEquals("MATCH p=()-->() RETURN p", new DepthOneStrategy().findAll());
    }

    @Test
    public void findByPropertyStringValue() throws Exception {
        assertEquals("MATCH p=(n:Asteroid)--(m) WHERE n.ref = '45 Eugenia' return p", new DepthOneStrategy().findByProperty("Asteroid", new Property<String, Object>("ref", "45 Eugenia")));
    }

    @Test
    public void findByPropertyIntegralValue() throws Exception {
        assertEquals("MATCH p=(n:Asteroid)--(m) WHERE n.index = 77 return p", new DepthOneStrategy().findByProperty("Asteroid", new Property<String, Object>("index", 77)));
    }

    @Test
    public void findByPropertyStandardForm() throws Exception {
        assertEquals("MATCH p=(n:Asteroid)--(m) WHERE n.diameter = 60.2 return p", new DepthOneStrategy().findByProperty("Asteroid", new Property<String, Object>("diameter", 6.02E1)));
    }

    @Test
    public void findByPropertyDecimal() throws Exception {
        assertEquals("MATCH p=(n:Asteroid)--(m) WHERE n.diameter = 60.2 return p", new DepthOneStrategy().findByProperty("Asteroid", new Property<String, Object>("diameter", 60.2)));
    }

    @Test
    public void findByPropertyEmbeddedDelimiter() throws Exception {
        assertEquals("MATCH p=(n:Cookbooks)--(m) WHERE n.title = 'Mrs Beeton\\'s Household Recipes' return p", new DepthOneStrategy().findByProperty("Cookbooks", new Property<String, Object>("title", "Mrs Beeton's Household Recipes")));
    }

    @Test
    public void delete() throws Exception {
        assertEquals("MATCH (n) WHERE id(n) = 123 OPTIONAL MATCH (n)-[r]-() DELETE r, n", new DepthOneStrategy().delete(123L));
    }

    @Test
    public void deleteAll() throws Exception {
        List<Long> ids = Arrays.asList(new Long[] { 123L, 234L, 345L });
        assertEquals("MATCH (n) WHERE id(n) in [123,234,345] OPTIONAL MATCH (n)-[r]-() DELETE r, n", new DepthOneStrategy().deleteAll(ids));
    }

    @Test
    public void deleteAllByLabel() throws Exception {
        assertEquals("MATCH (n:NODE) OPTIONAL MATCH (n)-[r]-() DELETE r, n", new DepthOneStrategy().deleteByLabel("NODE"));
    }

    @Test
    public void purge() throws Exception {
        assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n", new DepthOneStrategy().purge());
    }

    @Test
    public void testUpdateProperties() throws Exception {
        List<Property<String, Object>> properties = new ArrayList<>();
        properties.add(new Property<String, Object>("iProp", 42));
        properties.add(new Property<String, Object>("fProp", 3.1415928));
        properties.add(new Property<String, Object>("sProp", "Pie and the meaning of life"));
        assertEquals("MATCH (n) WHERE id(n) = 123 SET n.iProp=42,n.fProp=3.1415928,n.sProp=\\\"Pie and the meaning of life\\\"", new DepthOneStrategy().updateProperties(123L, properties));
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

        assertEquals("CREATE (n:NODE:VERTEX) SET n.iProp=42,n.fProp=3.1415928,n.sProp=\\\"Pie and the meaning of life\\\" return id(n)", new DepthOneStrategy().createNode(properties, labels));
    }
}
