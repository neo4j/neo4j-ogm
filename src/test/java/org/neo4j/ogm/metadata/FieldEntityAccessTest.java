package org.neo4j.ogm.metadata;

import org.junit.Test;
import org.neo4j.ogm.entityaccess.FieldEntityAccess;
import org.neo4j.ogm.mapper.domain.social.Individual;

import static org.junit.Assert.assertEquals;

public class FieldEntityAccessTest {

    @Test
    public void shouldDoNothingIfAskedToWriteValueToNull() throws Exception {
        new FieldEntityAccess("testProperty").set(null, "Arbitrary Value");
    }

    @Test
    public void shouldWriteScalarPropertyValuesToAppropriateFieldOfObject() throws Exception {
        Individual peter = new Individual();

        new FieldEntityAccess("name").set(peter, "Peter");
        new FieldEntityAccess("age").set(peter, 34);

        assertEquals("Peter", peter.getName());
        assertEquals(34, peter.getAge());
    }

}
