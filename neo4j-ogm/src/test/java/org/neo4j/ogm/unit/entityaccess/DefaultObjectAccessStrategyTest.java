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
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.DomainInfo;

public class DefaultObjectAccessStrategyTest {

    private DefaultObjectAccessStrategy objectAccessStrategy = new DefaultObjectAccessStrategy();
    private DomainInfo domainInfo = new DomainInfo("org.neo4j.ogm.unit.entityaccess",
            "org.neo4j.ogm.domain.forum", "org.neo4j.ogm.domain.satellites");

    @Test
    public void shouldReturnAnnotatedMethodInPreferenceToAnnotatedFieldWhenFindingPropertyToSet() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        ObjectAccess objectAccess = this.objectAccessStrategy.getPropertyWriteAccess(classInfo, "testAnnoProp");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);

        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, "Arbitrary Value");
        assertEquals("Arbitrary Value", domainObject.fullyAnnotatedProperty);
        assertTrue("The accessor method wasn't used to set the value", domainObject.fullyAnnotatedPropertyAccessorWasCalled);
    }

    @Test
    public void shouldReturnAnnotatedFieldInPreferenceToPlainMethodWhenFindingPropertyToSet() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        // testProp matches the setter/getter name but because the field is annotated then it should be used instead
        ObjectAccess objectAccess = this.objectAccessStrategy.getPropertyWriteAccess(classInfo, "testProp");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);

        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, "TEST");
        assertEquals("TEST", domainObject.annotatedTestProperty);
    }

    @Test
    public void shouldReturnAccessorMethodInPreferenceToFieldIfNoAnnotationsArePresent() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        ObjectAccess objectAccess = this.objectAccessStrategy.getPropertyWriteAccess(classInfo, "nonAnnotatedTestProperty");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);

        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, 8.14);
        assertEquals(8.14, domainObject.nonAnnotatedTestProperty, 0.0);
        assertTrue("The setter method wasn't called to write the value", domainObject.nonAnnotatedTestPropertyAccessorWasCalled);
    }

    @Test
    public void shouldReturnFieldCorrespondingToPropertyIfNoAnnotationsOrAccessorMethodsArePresent() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        ObjectAccess objectAccess = this.objectAccessStrategy.getPropertyWriteAccess(classInfo, "propertyWithoutAccessorMethods");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, 27);
        assertEquals(27, domainObject.propertyWithoutAccessorMethods);
    }

    @Test
    public void shouldRetrieveObjectAccessForWritingIterableObject() {
        ClassInfo classInfo = this.domainInfo.getClass(Program.class.getName());

        // TODO: this supports the behaviour required currently, but what happens if there's more than one collection of X?
        ObjectAccess iterableAccess = this.objectAccessStrategy.getIterableAccess(classInfo, Satellite.class);
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
        ObjectAccess objectAccess = this.objectAccessStrategy.getRelationshipAccess(classInfo, "SATELLITES", singleSatellite);
        assertNull("A compatible object accessor shouldn't have been found", objectAccess);
    }

    @Test
    public void shouldPreferAnnotatedMethodToAnnotatedFieldWhenSettingRelationshipObject() {
        // 1st, try to find a method annotated with the relationship type.
        ClassInfo classInfo = this.domainInfo.getClass(Member.class.getName());
        List<? extends Activity> parameter = Arrays.asList(new Comment());

        ObjectAccess objectAccess = this.objectAccessStrategy.getRelationshipAccess(classInfo, "HAS_ACTIVITY", parameter);
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

        ObjectAccess objectAccess = this.objectAccessStrategy.getRelationshipAccess(classInfo, "CONTAINS", parameter);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, parameter);
        assertEquals(domainObject.member, parameter);
        assertFalse("The access mechanism should be via the field", domainObject.containsAccessorWasCalled);
    }

    @Test
    public void shouldPreferSetterBasedOnRelationshipTypeNameToFieldInObjectWithoutAnnotations() {
        // 3rd, try to find a "setXYZ" method where XYZ is derived from the relationship type
        ClassInfo classInfo = this.domainInfo.getClass(Satellite.class.getName());

        Location satelliteLocation = new Location();
        satelliteLocation.setName("Outer Space");

        ObjectAccess objectAccess = this.objectAccessStrategy.getRelationshipAccess(classInfo, "LOCATION", satelliteLocation);
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

        ObjectAccess objectAccess = this.objectAccessStrategy.getRelationshipAccess(classInfo, "FAVOURITE_TOPIC", favouriteTopic);
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

        ObjectAccess objectAccess = this.objectAccessStrategy.getRelationshipAccess(classInfo, "DOES_NOT_MATCH", favouriteTopic);
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

        ObjectAccess objectAccess = this.objectAccessStrategy.getRelationshipAccess(classInfo, "UTTER_RUBBISH", forumPost);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, forumPost);
        assertEquals(domainObject.postWithoutAccessorMethods, forumPost);
    }

    /**
     * Domain object exhibiting various annotation configurations on its properties for test purposes.
     */
    public static class DummyDomainObject {

        // interestingly, if I extend DomainObject then the inherited ID field isn't found within a nested class
        @SuppressWarnings("unused")
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
        boolean containsAccessorWasCalled;

        Topic favouriteTopic;
        boolean topicAccessorWasCalled;

        Post postWithoutAccessorMethods;

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
            this.containsAccessorWasCalled = true;
            return member;
        }

        public void setContains(Member nestedObject) {
            this.containsAccessorWasCalled = true;
            this.member = nestedObject;
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
