package org.neo4j.ogm.mapper;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.ogm.entityaccess.FieldEntityAccessFactory;
import org.neo4j.ogm.mapper.domain.education.Course;
import org.neo4j.ogm.mapper.domain.education.Student;
import org.neo4j.ogm.metadata.dictionary.AttributeDictionary;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class ObjectToCypherMapperTest {

    private ObjectToCypherMapper mapper;

    @Rule
    public final JUnitRuleMockery mockery = new JUnitRuleMockery();

    @Mock
    private AttributeDictionary attributeDictionary;

    @Before
    public void setUpMapper() {
        this.mapper = new ObjectGraphMapper(Object.class, null, new FieldEntityAccessFactory(), this.attributeDictionary);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnAttemptToMapNullObjectToCypherQuery() {
        this.mapper.mapToCypher(null);
    }

    @Test
    public void shouldProduceCypherForCreatingNewSimpleObject() {
        Student newStudent = new Student();
        newStudent.setName("Gary");

        assertNull(newStudent.getId());

        this.mockery.checking(new Expectations() {
            {
                oneOf(attributeDictionary).lookUpValueAttributesFromType(Student.class);
                will(returnValue(new HashSet<>(Arrays.asList("id", "name"))));
                oneOf(attributeDictionary).lookUpPropertyNameForAttribute("id");
                will(returnValue("id"));
                oneOf(attributeDictionary).lookUpPropertyNameForAttribute("name");
                will(returnValue("forename"));
                oneOf(attributeDictionary).lookUpCompositeEntityAttributesFromType(Student.class);
                will(returnValue(Collections.emptySet()));
            }
        });

        List<String> cypher = this.mapper.mapToCypher(newStudent);
        assertNotNull("The resultant cypher shouldn't be null", cypher);
        assertFalse("The resultant list of cypher statements shouldn't be empty", cypher.isEmpty());
        System.out.println(cypher);
    }

    @Test
    public void shouldProduceCypherForUpdatingExistingSimpleObject() {
        Student newStudent = new Student();
        newStudent.setId(339L);
        newStudent.setName("Sheila");

        this.mockery.checking(new Expectations() {
            {
                oneOf(attributeDictionary).lookUpValueAttributesFromType(Student.class);
                will(returnValue(new HashSet<>(Arrays.asList("id", "name"))));
                oneOf(attributeDictionary).lookUpPropertyNameForAttribute("id");
                will(returnValue("id"));
                oneOf(attributeDictionary).lookUpPropertyNameForAttribute("name");
                will(returnValue("forename"));
                oneOf(attributeDictionary).lookUpCompositeEntityAttributesFromType(Student.class);
                will(returnValue(Collections.emptySet()));
            }
        });

        List<String> cypher = this.mapper.mapToCypher(newStudent);
        assertNotNull("The resultant cypher shouldn't be null", cypher);
        assertFalse("The resultant list of cypher statements shouldn't be empty", cypher.isEmpty());
        System.out.println(cypher);
    }

    @Test
    public void shouldProduceCypherForSmallGraphOfPersistentAndTransientObjects() {
        Student transientStudent = new Student();
        transientStudent.setName("Lakshmipathy");
        Student persistentStudent = new Student();
        persistentStudent.setId(103L);
        persistentStudent.setName("Giuseppe");
        Course existingCourse = new Course();
        existingCourse.setId(49L);
        existingCourse.setName("BSc Computer Science");
        existingCourse.setStudents(Arrays.asList(transientStudent, persistentStudent));

        this.mockery.checking(new Expectations() {
            {
                exactly(2).of(attributeDictionary).lookUpValueAttributesFromType(Student.class);
                will(returnValue(new HashSet<>(Arrays.asList("id", "name"))));
                oneOf(attributeDictionary).lookUpValueAttributesFromType(Course.class);
                will(returnValue(new HashSet<>(Arrays.asList("id", "name"))));
                allowing(attributeDictionary).lookUpPropertyNameForAttribute("id");
                will(returnValue("id"));
                allowing(attributeDictionary).lookUpPropertyNameForAttribute("name");
                will(returnValue("name"));
                exactly(2).of(attributeDictionary).lookUpCompositeEntityAttributesFromType(Student.class);
                will(returnValue(Collections.emptySet()));
                oneOf(attributeDictionary).lookUpCompositeEntityAttributesFromType(Course.class);
                will(returnValue(Collections.singleton("students")));
                oneOf(attributeDictionary).lookUpRelationshipTypeForAttribute("students");
                will(returnValue("HAS_STUDENT"));
            }
        });

        List<String> cypher = this.mapper.mapToCypher(existingCourse);
        assertNotNull("The resultant cypher shouldn't be null", cypher);
        assertFalse("The resultant list of cypher statements shouldn't be empty", cypher.isEmpty());
        System.out.println(cypher);
    }

}
