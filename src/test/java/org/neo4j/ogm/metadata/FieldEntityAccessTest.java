package org.neo4j.ogm.metadata;

import org.junit.Test;
import org.neo4j.ogm.entityaccess.FieldEntityAccess;
import org.neo4j.ogm.mapper.domain.education.Student;
import org.neo4j.ogm.mapper.domain.social.Individual;

import static org.junit.Assert.assertEquals;

public class FieldEntityAccessTest {

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionIfAskedToWriteValueToNullInstance() throws Exception {
        FieldEntityAccess.forProperty("testProperty").set(null, "Arbitrary Value");
    }

    @Test
    public void shouldWriteScalarPropertyValuesToAppropriateFieldOfObject() throws Exception {
        Individual peter = new Individual();

        FieldEntityAccess.forProperty("name").set(peter, "Peter");
        FieldEntityAccess.forProperty("age").set(peter, 34);

        assertEquals("Peter", peter.getName());
        assertEquals(34, peter.getAge());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnAttemptToRetrieveScalarValueFromFieldOfNullObject() {
        FieldEntityAccess.forProperty("doesn't matter").readValue(null);
    }

    @Test
    public void shouldRetrieveScalarValueFromField() {
        Individual toRead = new Individual();
        toRead.setId(9L);
        toRead.setName("Navdeep");
        toRead.setAge(25);

        Object readValue = FieldEntityAccess.forProperty("name").readValue(toRead);
        assertEquals(toRead.getName(), readValue);
    }

    @Test
    public void shouldRetrieveValuesOfFieldsFromSuperclasses() {
        Student student = new Student();
        student.setId(81L);
        student.setName("Colin");

        Object readValue = FieldEntityAccess.forProperty("id").readValue(student);
        assertEquals(student.getId(), readValue);
    }

}
