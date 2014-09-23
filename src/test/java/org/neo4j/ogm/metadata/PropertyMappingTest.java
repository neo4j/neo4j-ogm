package org.neo4j.ogm.metadata;

import static org.junit.Assert.*;

import org.junit.Test;
import org.neo4j.ogm.testmodel.Person;

public class PropertyMappingTest {

    @Test
    public void shouldDoNothingIfAskedToWriteValueToNull() {
        new PropertyMapping("testProperty", "Arbitrary Value").writeToObject(null);
    }

    @Test
    public void shouldWritePropertyValueToAppropriateFieldOfObject() {
        Person peter = new Person();

        new PropertyMapping("name", "Peter").writeToObject(peter);
        new PropertyMapping("age", 34).writeToObject(peter);

        assertEquals("Peter", peter.getName());
        assertEquals(34, peter.getAge());
    }

}
