package org.neo4j.ogm.unit.entityaccess;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.domain.forum.Member;
import org.neo4j.ogm.domain.forum.Topic;
import org.neo4j.ogm.domain.forum.activity.Activity;
import org.neo4j.ogm.domain.forum.activity.Comment;
import org.neo4j.ogm.domain.forum.activity.Post;
import org.neo4j.ogm.domain.satellites.Location;
import org.neo4j.ogm.domain.satellites.Program;
import org.neo4j.ogm.domain.satellites.Satellite;
import org.neo4j.ogm.entityaccess.DefaultObjectAccessStrategy;
import org.neo4j.ogm.entityaccess.MethodAccess;
import org.neo4j.ogm.entityaccess.ObjectAccess;
import org.neo4j.ogm.entityaccess.PropertyReader;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.DomainInfo;

public class DefaultObjectAccessStrategyTest {

    private DefaultObjectAccessStrategy objectAccessStrategy = new DefaultObjectAccessStrategy();
    private DomainInfo domainInfo = new DomainInfo("org.neo4j.ogm.unit.entityaccess",
            "org.neo4j.ogm.domain.forum", "org.neo4j.ogm.domain.satellites");

    @Test
    public void shouldPreferAnnotatedMethodToAnnotatedFieldWhenFindingPropertyToSet() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        ObjectAccess objectAccess = this.objectAccessStrategy.getPropertyWriter(classInfo, "testAnnoProp");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);

        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, "Arbitrary Value");
        assertEquals("Arbitrary Value", domainObject.fullyAnnotatedProperty);
        assertTrue("The accessor method wasn't used to set the value", domainObject.fullyAnnotatedPropertyAccessorWasCalled);
    }

    @Test
    public void shouldPreferAnnotatedFieldToPlainMethodWhenFindingPropertyToSet() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        // testProp matches the setter/getter name but because the field is annotated then it should be used instead
        ObjectAccess objectAccess = this.objectAccessStrategy.getPropertyWriter(classInfo, "testProp");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);

        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, "TEST");
        assertEquals("TEST", domainObject.annotatedTestProperty);
    }

    @Test
    public void shouldReturnAccessorMethodInPreferenceToFieldIfNoAnnotationsArePresent() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        ObjectAccess objectAccess = this.objectAccessStrategy.getPropertyWriter(classInfo, "nonAnnotatedTestProperty");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);

        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, 8.14);
        assertEquals(8.14, domainObject.nonAnnotatedTestProperty, 0.0);
        assertTrue("The setter method wasn't called to write the value", domainObject.nonAnnotatedTestPropertyAccessorWasCalled);
    }

    @Test
    public void shouldAccessViaFieldCorrespondingToPropertyIfNoAnnotationsOrAccessorMethodsArePresent() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.propertyWithoutAccessorMethods = 9;

        // test writing via field
        ObjectAccess writer = this.objectAccessStrategy.getPropertyWriter(classInfo, "propertyWithoutAccessorMethods");
        assertNotNull("The resultant writer shouldn't be null", writer);
        writer.write(domainObject, 27);
        assertEquals(27, domainObject.propertyWithoutAccessorMethods);

        // test reading via field
        PropertyReader reader = this.objectAccessStrategy.getPropertyReader(classInfo, "propertyWithoutAccessorMethods");
        assertNotNull("The resultant reader shouldn't be null", reader);
        assertEquals(domainObject.propertyWithoutAccessorMethods, reader.read(domainObject));
   }

    @Test
    public void shouldRetrieveObjectAccessForWritingIterableObject() {
        ClassInfo classInfo = this.domainInfo.getClass(Program.class.getName());

        // TODO: this supports the behaviour required currently, but what happens if there's more than one collection of X?
        ObjectAccess iterableAccess = this.objectAccessStrategy.getIterableWriter(classInfo, Satellite.class);
        assertNotNull("The resultant object accessor shouldn't be null", iterableAccess);
        Program spaceProgramme = new Program();
        iterableAccess.write(spaceProgramme, Arrays.asList(new Satellite()));
        assertNotNull("The satellites list wasn't set correctly", spaceProgramme.getSatellites());
        assertFalse("The satellites list wasn't set correctly", spaceProgramme.getSatellites().isEmpty());
    }

    @Test
    public void shouldNotRetrieveSetterMethodObjectAccessIfTypesAreIncompatible() {
        ClassInfo classInfo = this.domainInfo.getClass(Program.class.getName());

        Satellite singleSatellite = new Satellite();

        // the SATELLITES type matches the setter that takes an Iterable argument
        ObjectAccess objectAccess = this.objectAccessStrategy.getRelationalWriter(classInfo, "SATELLITES", singleSatellite);
        assertNull("A compatible object accessor shouldn't have been found", objectAccess);
    }

    @Test
    public void shouldPreferAnnotatedMethodToAnnotatedFieldWhenSettingRelationshipObject() {
        // 1st, try to find a method annotated with the relationship type.
        ClassInfo classInfo = this.domainInfo.getClass(Member.class.getName());
        List<? extends Activity> parameter = Arrays.asList(new Comment());

        ObjectAccess objectAccess = this.objectAccessStrategy.getRelationalWriter(classInfo, "HAS_ACTIVITY", parameter);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertTrue("The access mechanism should be via the setter", objectAccess instanceof MethodAccess);
        Member member = new Member();
        objectAccess.write(member, parameter);
        assertEquals(member.getActivityList(), parameter);
    }

    @Test
    public void shouldPreferAnnotatedFieldToPlainSetterMatchingRelationshipTypeNameWhenSettingRelationshipObject() {
        // 2nd, try to find a field annotated with with relationship type
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        Member parameter = new Member();

        ObjectAccess objectAccess = this.objectAccessStrategy.getRelationalWriter(classInfo, "CONTAINS", parameter);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, parameter);
        assertEquals(domainObject.member, parameter);
    }

    @Test
    public void shouldPreferSetterBasedOnRelationshipTypeNameToFieldInObjectWithoutAnnotations() {
        // 3rd, try to find a "setXYZ" method where XYZ is derived from the relationship type
        ClassInfo classInfo = this.domainInfo.getClass(Satellite.class.getName());

        Location satelliteLocation = new Location();
        satelliteLocation.setName("Outer Space");

        ObjectAccess objectAccess = this.objectAccessStrategy.getRelationalWriter(classInfo, "LOCATION", satelliteLocation);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertTrue("The access mechanism should be via the setter", objectAccess instanceof MethodAccess);
        Satellite satellite = new Satellite();
        objectAccess.write(satellite, satelliteLocation);
        assertEquals(satellite.getLocation(), satelliteLocation);
    }

    @Test
    public void shouldPreferFieldBasedOnRelationshipTypeNameToPlainSetterWithMatchingParameterType() {
        // 4th, try to find a "XYZ" field name where XYZ is derived from the relationship type
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());
        Topic favouriteTopic = new Topic();

        // NB: the setter is called setTopic here, so a relationship type of just "TOPIC" would choose the setter
        ObjectAccess objectAccess = this.objectAccessStrategy.getRelationalWriter(classInfo, "FAVOURITE_TOPIC", favouriteTopic);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, favouriteTopic);
        assertEquals(domainObject.favouriteTopic, favouriteTopic);
        assertFalse("The access should be via the field", domainObject.topicAccessorWasCalled);
    }

    @Test
    public void shouldDefaultToFindingSetterThatMatchesTheParameterTypeIfRelationshipTypeCannotBeMatched() {
        // 5th, try to find a single setter that takes the parameter
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());
        Topic favouriteTopic = new Topic();

        ObjectAccess objectAccess = this.objectAccessStrategy.getRelationalWriter(classInfo, "DOES_NOT_MATCH", favouriteTopic);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, favouriteTopic);
        assertEquals(domainObject.favouriteTopic, favouriteTopic);
        assertTrue("The access should be via the setter method", domainObject.topicAccessorWasCalled);
    }

    @Test
    public void shouldDefaultToFieldThatMatchesTheParameterTypeIfRelationshipTypeCannotBeMatchedAndNoSetterExists() {
        // 6th, try to find a field that shares the same type as the parameter
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());
        Post forumPost = new Post();

        ObjectAccess objectAccess = this.objectAccessStrategy.getRelationalWriter(classInfo, "UTTER_RUBBISH", forumPost);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, forumPost);
        assertEquals(domainObject.postWithoutAccessorMethods, forumPost);
    }

    @Test
    public void shouldPreferAnnotatedMethodToAnnotatedFieldWhenReadingFromAnObject() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.fullyAnnotatedProperty = "test text";

        PropertyReader objectAccess = this.objectAccessStrategy.getPropertyReader(classInfo, "testAnnoProp");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertEquals(domainObject.fullyAnnotatedProperty, objectAccess.read(domainObject));
        assertTrue("The accessor method wasn't used to get the value", domainObject.fullyAnnotatedPropertyAccessorWasCalled);
    }

    @Test
    public void shouldPreferAnnotatedFieldToPlainGetterWhenReadingFromAnObject() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.annotatedTestProperty = "more arbitrary text";

        PropertyReader objectAccess = this.objectAccessStrategy.getPropertyReader(classInfo, "testProp");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertEquals(domainObject.annotatedTestProperty, objectAccess.read(domainObject));
    }

    @Test
    public void shouldPreferMethodBasedAccessToFieldAccessWhenReadingFromObjectsWithoutAnnotations() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.nonAnnotatedTestProperty = new Double(30.16);

        PropertyReader objectAccess = this.objectAccessStrategy.getPropertyReader(classInfo, "nonAnnotatedTestProperty");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertEquals(domainObject.nonAnnotatedTestProperty, objectAccess.read(domainObject));
    }

    /**
     * Domain object exhibiting various annotation configurations on its properties for test purposes.
     */
    public static class DummyDomainObject {

        // interestingly, if I extend DomainObject then the inherited ID field isn't found within a nested class
        private Long id;

        @Property(name = "testProp")
        String annotatedTestProperty;

        Double nonAnnotatedTestProperty;
        boolean nonAnnotatedTestPropertyAccessorWasCalled;

        @Property(name = "testAnnoProp")
        String fullyAnnotatedProperty;
        boolean fullyAnnotatedPropertyAccessorWasCalled;

        int propertyWithoutAccessorMethods;

        @Relationship(type = "CONTAINS")
        Member member;

        Topic favouriteTopic;
        boolean topicAccessorWasCalled;

        Post postWithoutAccessorMethods;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setTestProp(String value) {
            throw new UnsupportedOperationException("Shouldn't be calling the setter with: " + value);
        }

        public String getTestProp() {
            throw new UnsupportedOperationException("Shouldn't be calling the getter");
        }

        public void setNonAnnotatedTestProperty(Double value) {
            this.nonAnnotatedTestPropertyAccessorWasCalled = true;
            this.nonAnnotatedTestProperty = value;
        }

        public Double getNonAnnotatedTestProperty() {
            this.nonAnnotatedTestPropertyAccessorWasCalled = true;
            return this.nonAnnotatedTestProperty;
        }

        @Property(name = "testAnnoProp")
        public String getFullyAnnotatedProperty() {
            this.fullyAnnotatedPropertyAccessorWasCalled = true;
            return fullyAnnotatedProperty;
        }

        @Property(name = "testAnnoProp")
        public void setFullyAnnotatedProperty(String fullyAnnotatedProperty) {
            this.fullyAnnotatedPropertyAccessorWasCalled = true;
            this.fullyAnnotatedProperty = fullyAnnotatedProperty;
        }

        public Member getContains() {
            throw new UnsupportedOperationException("Shouldn't be calling the getter");
        }

        public void setContains(Member nestedObject) {
            throw new UnsupportedOperationException("Shouldn't be calling the setter with: " + nestedObject);
        }

        public Topic getTopic() {
            this.topicAccessorWasCalled = true;
            return favouriteTopic;
        }

        public void setTopic(Topic favouriteTopic) {
            this.topicAccessorWasCalled = true;
            this.favouriteTopic = favouriteTopic;
        }

    }

}
