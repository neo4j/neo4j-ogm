package org.neo4j.ogm.session.strategy;

import org.neo4j.graphmodel.Property;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class VariableDepthReadStrategyTest {

    private final VariableDepthReadStrategy strategy = new VariableDepthReadStrategy();

    @Test
    public void testFindOne() throws Exception {
        assertEquals("MATCH p=(n)-[*1..2]-(m) WHERE id(n) = { id } RETURN collect(distinct p)", strategy.findOne(0L, 2).getStatement());
    }

    @Test
    public void testFindAllCollection() throws Exception {
        assertEquals("MATCH p=(n)-[*1..1]-(m) WHERE id(n) in { ids } RETURN collect(distinct p)", strategy.findAll(Arrays.asList(1L, 2L, 3L), 1).getStatement());
    }

    @Test
    public void testFindAll() throws Exception {
        assertEquals("MATCH p=()-->() RETURN p", strategy.findAll().getStatement());
    }

    @Test
    public void testFindByLabel() throws Exception {
        assertEquals("MATCH p=(n:Orbit)-[*1..3]-(m) RETURN collect(distinct p)", strategy.findByLabel("Orbit", 3).getStatement());
    }

    @Test
    public void testFindByProperty() throws Exception {
        assertEquals("MATCH p=(n:Asteroid)-[*1..4]-(m) WHERE n.diameter = { diameter } RETURN collect(distinct p)", strategy.findByProperty("Asteroid", new Property<String, Object>("diameter", 60.2), 4).getStatement());
    }

    @Test
    public void testFindOneZeroDepth() throws Exception {

    }

    @Test
    public void testFindAllCollectionZeroDepth() throws Exception {

    }

    @Test
    public void testFindAllZeroDepth() throws Exception {

    }

    @Test
    public void testFindByLabelZeroDepth() throws Exception {

    }

    @Test
    public void testFindByPropertyZeroDepth() throws Exception {

    }


}
