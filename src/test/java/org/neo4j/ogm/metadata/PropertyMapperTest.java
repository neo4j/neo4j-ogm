package org.neo4j.ogm.metadata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.neo4j.ogm.domain.social.Person;

public class PropertyMapperTest {

    @Test
    public void shouldDoNothingIfAskedToWriteValueToNull() {
        new FieldBasedPropertyMapper("testProperty", "Arbitrary Value").writeToObject(null);
    }

    @Test
    public void shouldWriteSimplePropertyValuesToAppropriateFieldOfObject() {
        Person peter = new Person();

        new FieldBasedPropertyMapper("name", "Peter").writeToObject(peter);
        new FieldBasedPropertyMapper("age", 34).writeToObject(peter);

        assertEquals("Peter", peter.getName());
        assertEquals(34, peter.getAge());
    }

}
