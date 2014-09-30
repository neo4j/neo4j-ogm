package org.neo4j.ogm.metadata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.neo4j.ogm.mapper.domain.social.Person;
import org.neo4j.ogm.strategy.FieldEntityAccess;

public class FieldEntityAccessTest {

    @Test
    public void shouldDoNothingIfAskedToWriteValueToNull() throws Exception {
        new FieldEntityAccess("testProperty").set(null, "Arbitrary Value");
    }

    @Test
    public void shouldWriteScalarPropertyValuesToAppropriateFieldOfObject() throws Exception {
        Person peter = new Person();

        new FieldEntityAccess("name").set(peter, "Peter");
        new FieldEntityAccess("age").set(peter, 34);

        assertEquals("Peter", peter.getName());
        assertEquals(34, peter.getAge());
    }

}
