package org.neo4j.ogm.metadata;

import org.junit.Test;
import org.neo4j.ogm.entityaccess.EntityAccess;
import org.neo4j.ogm.entityaccess.FieldEntityAccess;
import org.neo4j.ogm.entityaccess.FieldEntityAccessFactory;
import org.neo4j.ogm.mapper.domain.education.Student;
import org.neo4j.ogm.mapper.domain.social.Individual;
import org.neo4j.ogm.metadata.dictionary.FieldDictionary;
import org.neo4j.ogm.metadata.info.DomainInfo;
import org.neo4j.ogm.strategy.simple.SimpleFieldDictionary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FieldEntityAccessTest {

    private FieldDictionary socialDictionary = new SimpleFieldDictionary(new DomainInfo("org.neo4j.ogm.mapper.domain.social"));
    private FieldDictionary educationDictionary = new SimpleFieldDictionary(new DomainInfo("org.neo4j.ogm.mapper.domain.education"));


    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionIfAskedToWriteValueToNullInstance() throws Exception {
        FieldEntityAccess.forProperty(socialDictionary, "testProperty").set(null, "Arbitrary Value");
    }

    @Test
    public void shouldWriteScalarPropertyValuesToAppropriateFieldOfObject() throws Exception {
        Individual peter = new Individual();


        FieldEntityAccess.forProperty(socialDictionary, "name").set(peter, "Peter");
        FieldEntityAccess.forProperty(socialDictionary, "age").set(peter, 34);

        assertEquals("Peter", peter.getName());
        assertEquals(34, peter.getAge());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnAttemptToRetrieveScalarValueFromFieldOfNullObject() {
        FieldEntityAccess.forProperty(socialDictionary, "doesn't matter").readValue(null);
    }

    @Test
    public void shouldRetrieveScalarValueFromField() {
        Individual toRead = new Individual();
        toRead.setId(9L);
        toRead.setName("Navdeep");
        toRead.setAge(25);

        Object readValue = FieldEntityAccess.forProperty(socialDictionary, "name").readValue(toRead);
        assertEquals(toRead.getName(), readValue);
    }

    @Test
    public void shouldRetrieveValuesOfFieldsFromSuperclasses() {
        Student student = new Student();
        student.setId(81L);
        student.setName("Colin");

        Object readValue = FieldEntityAccess.forProperty(educationDictionary, "id").readValue(student);
        assertEquals(student.getId(), readValue);
    }

    @Test
    public void shouldManufactureEntityAccessForSpecifiedFieldOfType() {
        FieldEntityAccessFactory entityAccessFactory = new FieldEntityAccessFactory(socialDictionary);

        Individual person = new Individual();
        person.setName("Gary");

        EntityAccess entityAccess = entityAccessFactory.forAttributeOfType("name", Individual.class);
        assertNotNull("The resultant EntityAccess shouldn't be null", entityAccess);
        assertEquals("The field value wasn't retrieved as expected", person.getName(), entityAccess.readValue(person));
    }

}
