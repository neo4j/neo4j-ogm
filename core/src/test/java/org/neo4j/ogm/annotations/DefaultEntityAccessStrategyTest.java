/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.annotations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.domain.forum.ForumTopicLink;
import org.neo4j.ogm.domain.forum.Member;
import org.neo4j.ogm.domain.forum.Topic;
import org.neo4j.ogm.domain.forum.activity.Activity;
import org.neo4j.ogm.domain.forum.activity.Comment;
import org.neo4j.ogm.domain.forum.activity.Post;
import org.neo4j.ogm.domain.satellites.Location;
import org.neo4j.ogm.domain.satellites.Program;
import org.neo4j.ogm.domain.satellites.Satellite;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Adam George
 * @author Luanne Misquitta
 */
public class DefaultEntityAccessStrategyTest {

    private DefaultEntityAccessStrategy entityAccessStrategy;
    private DomainInfo domainInfo;

    @Before
    public void setup() {
        entityAccessStrategy = new DefaultEntityAccessStrategy();
        domainInfo = new DomainInfo(
                "org.neo4j.ogm.domain.forum",
                "org.neo4j.ogm.domain.satellites",
                "org.neo4j.ogm.annotations"
        );
    }

    @Test
    public void shouldPreferAnnotatedMethodToAnnotatedFieldWhenFindingPropertyToSet() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        EntityAccess objectAccess = this.entityAccessStrategy.getPropertyWriter(classInfo, "testAnnoProp");
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
        EntityAccess objectAccess = this.entityAccessStrategy.getPropertyWriter(classInfo, "testProp");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);

        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, "TEST");
        assertEquals("TEST", domainObject.annotatedTestProperty);
    }

    /**
     * @see DATAGRAPH-674
     */
    @Test
    public void shouldPreferAnnotatedFieldToMethodNotAnnotatedWithPropertyWhenFindingPropertyToSet() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());
        DummyDomainObject domainObject = new DummyDomainObject();

        EntityAccess objectAccess = this.entityAccessStrategy.getPropertyWriter(classInfo, "testIgnored");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertTrue(objectAccess instanceof FieldWriter);
        assertEquals(String.class, objectAccess.type());
        objectAccess.write(domainObject, "TEST");
        assertEquals("TEST", domainObject.propertyMethodsIgnored);
    }


    @Test
    public void shouldReturnAccessorMethodInPreferenceToFieldIfNoAnnotationsArePresent() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        EntityAccess objectAccess = this.entityAccessStrategy.getPropertyWriter(classInfo, "nonAnnotatedTestProperty");
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
        EntityAccess writer = this.entityAccessStrategy.getPropertyWriter(classInfo, "propertyWithoutAccessorMethods");
        assertNotNull("The resultant writer shouldn't be null", writer);
        writer.write(domainObject, 27);
        assertEquals(27, domainObject.propertyWithoutAccessorMethods);

        // test reading via field
        PropertyReader reader = this.entityAccessStrategy.getPropertyReader(classInfo, "propertyWithoutAccessorMethods");
        assertNotNull("The resultant reader shouldn't be null", reader);
        assertEquals(domainObject.propertyWithoutAccessorMethods, reader.read(domainObject));
    }

    @Test
    public void shouldRetrieveObjectAccessForWritingIterableObject() {
        ClassInfo classInfo = this.domainInfo.getClass(Program.class.getName());

        RelationalWriter iterableAccess = this.entityAccessStrategy.getIterableWriter(classInfo, Satellite.class, "satellites", Relationship.OUTGOING);
        assertNotNull("The resultant object accessor shouldn't be null", iterableAccess);
        Program spaceProgramme = new Program();
        iterableAccess.write(spaceProgramme, Arrays.asList(new Satellite()));
        assertNotNull("The satellites list wasn't set correctly", spaceProgramme.getSatellites());
        assertFalse("The satellites list wasn't set correctly", spaceProgramme.getSatellites().isEmpty());
    }

    @Test
    @Ignore // we do expect this to work now.
    public void shouldNotRetrieveSetterMethodObjectAccessIfTypesAreIncompatible() {
        ClassInfo classInfo = this.domainInfo.getClass(Program.class.getName());

        Satellite singleSatellite = new Satellite();

        // the SATELLITES type matches the setter that takes an Iterable argument
        RelationalWriter objectAccess = this.entityAccessStrategy.getRelationalWriter(classInfo, "SATELLITES", Relationship.OUTGOING, singleSatellite);
        assertNull("A compatible object accessor shouldn't have been found", objectAccess);
    }

    @Test
    public void shouldPreferAnnotatedMethodToAnnotatedFieldWhenSettingRelationshipObject() {
        // 1st, try to find a method annotated with the relationship type.
        ClassInfo classInfo = this.domainInfo.getClass(Member.class.getName());
        List<? extends Activity> parameter = Arrays.asList(new Comment());

        RelationalWriter objectAccess = this.entityAccessStrategy.getRelationalWriter(classInfo, "HAS_ACTIVITY", Relationship.OUTGOING, new Comment());
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertTrue("The access mechanism should be via the setter", objectAccess instanceof MethodWriter);
        Member member = new Member();
        objectAccess.write(member, parameter);
        assertEquals(member.getActivityList(), parameter);
    }

    @Test
    public void shouldPreferAnnotatedFieldToPlainSetterMatchingRelationshipTypeWhenSettingRelationshipObject() {
        // 2nd, try to find a field annotated with with relationship type
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        Member parameter = new Member();

        RelationalWriter objectAccess = this.entityAccessStrategy.getRelationalWriter(classInfo, "CONTAINS", Relationship.OUTGOING, parameter);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, parameter);
        assertEquals(domainObject.member, parameter);

        Member otherMember = new Member();
        objectAccess = this.entityAccessStrategy.getRelationalWriter(classInfo, "REGISTERED", Relationship.OUTGOING, otherMember);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, otherMember);
        assertEquals(domainObject.registeredMember, otherMember);
    }

    @Test
    public void shouldPreferSetterBasedOnRelationshipTypeToFieldInObjectWithoutAnnotations() {
        // 3rd, try to find a "setXYZ" method where XYZ is derived from the relationship type
        ClassInfo classInfo = this.domainInfo.getClass(Satellite.class.getName());

        Location satelliteLocation = new Location();
        satelliteLocation.setName("Outer Space");

        RelationalWriter objectAccess = this.entityAccessStrategy.getRelationalWriter(classInfo, "LOCATION", Relationship.OUTGOING, satelliteLocation);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertTrue("The access mechanism should be via the setter", objectAccess instanceof MethodWriter);
        Satellite satellite = new Satellite();
        objectAccess.write(satellite, satelliteLocation);
        assertEquals(satellite.getLocation(), satelliteLocation);
    }

    @Test
    public void shouldPreferFieldBasedOnRelationshipTypeToPlainSetterWithMatchingParameterType() {
        // 4th, try to find a "XYZ" field name where XYZ is derived from the relationship type
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());
        Topic favouriteTopic = new Topic();

        // NB: the setter is called setTopic here, so a relationship type of just "TOPIC" would choose the setter
        RelationalWriter objectAccess = this.entityAccessStrategy.getRelationalWriter(classInfo, "FAVOURITE_TOPIC", Relationship.OUTGOING, favouriteTopic);
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

        RelationalWriter objectAccess = this.entityAccessStrategy.getRelationalWriter(classInfo, "DOES_NOT_MATCH", Relationship.OUTGOING, favouriteTopic);
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

        RelationalWriter objectAccess = this.entityAccessStrategy.getRelationalWriter(classInfo, "UTTER_RUBBISH", Relationship.OUTGOING, forumPost);
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

        PropertyReader objectAccess = this.entityAccessStrategy.getPropertyReader(classInfo, "testAnnoProp");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertEquals(domainObject.fullyAnnotatedProperty, objectAccess.read(domainObject));
        assertTrue("The accessor method wasn't used to get the value", domainObject.fullyAnnotatedPropertyAccessorWasCalled);
    }

    @Test
    public void shouldPreferAnnotatedFieldToPlainGetterWhenReadingFromAnObject() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.annotatedTestProperty = "more arbitrary text";

        PropertyReader objectAccess = this.entityAccessStrategy.getPropertyReader(classInfo, "testProp");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertEquals(domainObject.annotatedTestProperty, objectAccess.read(domainObject));
    }

    @Test
    public void shouldPreferAnnotatedFieldToGetterWhenReadingFromAnObject() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.propertyWithDifferentAnnotatedGetter = "more arbitrary text";
        Collection<PropertyReader> readers = this.entityAccessStrategy.getPropertyReaders(classInfo);


        PropertyReader objectAccess = this.entityAccessStrategy.getPropertyReader(classInfo, "differentAnnotationOnGetter");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertEquals(domainObject.propertyWithDifferentAnnotatedGetter, objectAccess.read(domainObject));

        for (PropertyReader reader : readers) {
            if (reader.propertyName().equals("differentAnnotationOnGetter")) {
                assertTrue(reader instanceof FieldReader);
            }
        }
    }

    @Test
    public void shouldPreferMethodBasedAccessToFieldAccessWhenReadingFromObjectsWithoutAnnotations() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.nonAnnotatedTestProperty = new Double(30.16);

        PropertyReader objectAccess = this.entityAccessStrategy.getPropertyReader(classInfo, "nonAnnotatedTestProperty");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertEquals(domainObject.nonAnnotatedTestProperty, objectAccess.read(domainObject));
    }

    @Test
    public void shouldPreferAnnotatedMethodToAnnotatedFieldMatchingRelationshipTypeWhenReadingRelationshipObject() {
        ClassInfo classInfo = this.domainInfo.getClass(Member.class.getName());
        Member member = new Member();
        member.setActivityList(Arrays.<Activity>asList(new Comment()));

        RelationalReader reader = this.entityAccessStrategy.getRelationalReader(classInfo, "HAS_ACTIVITY", Relationship.OUTGOING);
        assertNotNull("The resultant object reader shouldn't be null", reader);
        assertTrue("The access mechanism should be via the getter", reader instanceof MethodReader);
        assertSame(member.getActivityList(), reader.read(member));
        assertEquals("HAS_ACTIVITY", reader.relationshipType());
    }

    @Test
    public void shouldPreferAnnotatedFieldToPlainGetterMethodMatchingRelationshipType() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.member = new Member();
        domainObject.registeredMember = new Member();

        RelationalReader reader = this.entityAccessStrategy.getRelationalReader(classInfo, "CONTAINS", Relationship.OUTGOING);
        assertNotNull("The resultant object reader shouldn't be null", reader);
        assertSame(domainObject.member, reader.read(domainObject));
        assertEquals("CONTAINS", reader.relationshipType());

        reader = this.entityAccessStrategy.getRelationalReader(classInfo, "REGISTERED", Relationship.OUTGOING);
        assertNotNull("The resultant object reader shouldn't be null", reader);
        assertSame(domainObject.registeredMember, reader.read(domainObject));
        assertEquals("REGISTERED", reader.relationshipType());

    }

    @Test
    public void shouldPreferGetterBasedOnRelationshipTypeToFieldInObjectWithoutAnnotations() {
        ClassInfo classInfo = this.domainInfo.getClass(Satellite.class.getName());

        Satellite satellite = new Satellite();
        Location satelliteLocation = new Location();
        satelliteLocation.setName("Outer Space");
        satellite.setLocation(satelliteLocation);

        RelationalReader reader = this.entityAccessStrategy.getRelationalReader(classInfo, "LOCATION", Relationship.OUTGOING);
        assertNotNull("The resultant object accessor shouldn't be null", reader);
        assertTrue("The access mechanism should be via the getter", reader instanceof MethodReader);
        assertSame(satellite.getLocation(), reader.read(satellite));
        assertEquals("LOCATION", reader.relationshipType());
    }

    @Test
    public void shouldReadFromFieldMatchingRelationshipTypeInObjectWithoutAnnotationsOrAccessorMethods() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.postWithoutAccessorMethods = new Post();

        RelationalReader reader = this.entityAccessStrategy.getRelationalReader(classInfo, "POST_WITHOUT_ACCESSOR_METHODS", Relationship.OUTGOING);
        assertNotNull("The resultant object accessor shouldn't be null", reader);
        assertSame(domainObject.postWithoutAccessorMethods, reader.read(domainObject));
        assertEquals("POST_WITHOUT_ACCESSOR_METHODS", reader.relationshipType());
    }

    @Test
    public void shouldUseFieldAccessUnconditionallyForReadingIdentityProperty() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        final long id = 593L;
        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.setId(id);

        PropertyReader idReader = this.entityAccessStrategy.getIdentityPropertyReader(classInfo);
        assertNotNull("The resultant ID reader shouldn't be null", idReader);
        assertEquals(id, idReader.read(domainObject));
    }

    @Test
    public void shouldRetrieveAppropriateObjectAccessToAllRelationalAttributesForParticularClass() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.postWithoutAccessorMethods = new Post();
        domainObject.favouriteTopic = new Topic();
        domainObject.member = new Member();
        domainObject.readOnlyComment = new Comment();
        domainObject.registeredMember = new Member();
        domainObject.naturalSatellites = new ArrayList<>();
        domainObject.artificialSatellites = Collections.singletonList(new Satellite());

        Collection<RelationalReader> relationalAccessors = this.entityAccessStrategy.getRelationalReaders(classInfo);
        assertNotNull("The resultant list of object accessors shouldn't be null", relationalAccessors);
        assertEquals("An unexpected number of accessors was returned", 7, relationalAccessors.size());

        Map<String, Class<? extends RelationalReader>> expectedRelationalReaders = new HashMap<>();
        expectedRelationalReaders.put("COMMENT", MethodReader.class);
        expectedRelationalReaders.put("FAVOURITE_TOPIC", FieldReader.class);
        expectedRelationalReaders.put("CONTAINS", FieldReader.class);
        expectedRelationalReaders.put("POST_WITHOUT_ACCESSOR_METHODS", FieldReader.class);
        expectedRelationalReaders.put("NATURAL", FieldReader.class);
        expectedRelationalReaders.put("ARTIFICIAL", FieldReader.class);
        expectedRelationalReaders.put("REGISTERED", FieldReader.class);

        for (RelationalReader objectAccess : relationalAccessors) {
            String relType = objectAccess.relationshipType();
            assertTrue("Relationship type " + relType + " wasn't expected", expectedRelationalReaders.containsKey(relType));
            assertEquals(expectedRelationalReaders.get(relType), objectAccess.getClass());
            assertNotNull(objectAccess.read(domainObject));
        }
    }

    @Test
    public void shouldRetrieveAppropriateObjectAccessToEndNodeAttributeOnRelationshipEntity() {
        ClassInfo relationshipEntityClassInfo = domainInfo.getClass(ForumTopicLink.class.getName());

        RelationalReader endNodeReader = this.entityAccessStrategy.getEndNodeReader(relationshipEntityClassInfo);
        assertNotNull("The resultant end node reader shouldn't be null", endNodeReader);

        ForumTopicLink forumTopicLink = new ForumTopicLink();
        Topic topic = new Topic();
        forumTopicLink.setTopic(topic);
        assertSame("The value wasn't read correctly", topic, endNodeReader.read(forumTopicLink));
    }

    @Test
    public void shouldReturnNullOnAttemptToAccessNonExistentEndNodeAttributeOnRelationshipEntity() {
        ClassInfo classInfoOfNonRelationshipEntity = domainInfo.getClass(Member.class.getName());
        assertNull(this.entityAccessStrategy.getEndNodeReader(classInfoOfNonRelationshipEntity));
    }

    /**
     * @see DATAGRAPH-637
     */
    @Test
    public void shouldPreferAnnotatedFieldWithMatchingRelationshipTypeWhenGettingIterableWriter() {
        // 2nd, try to find a field annotated with with relationship type
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        List<Satellite> natural = new ArrayList<>();
        natural.add(new Satellite());

        RelationalWriter objectAccess = this.entityAccessStrategy.getIterableWriter(classInfo, Satellite.class, "NATURAL", Relationship.OUTGOING);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, natural);
        assertEquals(natural, domainObject.naturalSatellites);
    }


    /**
     * @see DATAGRAPH-637
     */
    @Test
    public void shouldPreferAnnotatedFieldWithMatchingRelationshipTypeWhenGettingIterableReader() {
        // 2nd, try to find a field annotated with with relationship type
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        List<Satellite> natural = new ArrayList<>();
        natural.add(new Satellite());

        RelationalReader relationalReader = this.entityAccessStrategy.getIterableReader(classInfo, Satellite.class, "NATURAL", Relationship.OUTGOING);
        assertNotNull("The resultant object accessor shouldn't be null", relationalReader);
        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.naturalSatellites = natural;
        Object o = relationalReader.read(domainObject);
        assertEquals(natural, o);
    }

    /**
     * Domain object exhibiting various annotation configurations on its properties for test purposes.
     */
    public static class DummyDomainObject {

        @Property(name = "testProp")
        String annotatedTestProperty;
        Double nonAnnotatedTestProperty;
        boolean nonAnnotatedTestPropertyAccessorWasCalled;
        @Property(name = "testAnnoProp")
        String fullyAnnotatedProperty;
        @Property(name = "testIgnored")
        String propertyMethodsIgnored;
        @Property(name = "differentAnnotationOnGetter")
        String propertyWithDifferentAnnotatedGetter;
        boolean fullyAnnotatedPropertyAccessorWasCalled;
        int propertyWithoutAccessorMethods;
        @Relationship(type = "CONTAINS")
        Member member;
        @Relationship(type = "REGISTERED")
        Member registeredMember;
        @Relationship(type = "NATURAL")
        List<Satellite> naturalSatellites;
        @Relationship(type = "ARTIFICIAL")
        List<Satellite> artificialSatellites;
        Topic favouriteTopic;
        boolean topicAccessorWasCalled;
        Post postWithoutAccessorMethods;
        Comment readOnlyComment;
        // interestingly, if I extend DomainObject then the inherited ID field isn't found within a nested class
        @SuppressWarnings("unused")
        private Long id;

        public Long getId() {
            throw new UnsupportedOperationException("Shouldn't be calling the ID getter");
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTestProp() {
            throw new UnsupportedOperationException("Shouldn't be calling the getter");
        }

        public void setTestProp(String value) {
            throw new UnsupportedOperationException("Shouldn't be calling the setter with: " + value);
        }

        public Double getNonAnnotatedTestProperty() {
            this.nonAnnotatedTestPropertyAccessorWasCalled = true;
            return this.nonAnnotatedTestProperty;
        }

        public void setNonAnnotatedTestProperty(Double value) {
            this.nonAnnotatedTestPropertyAccessorWasCalled = true;
            this.nonAnnotatedTestProperty = value;
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

        public Member getRegisteredMember() {
            throw new UnsupportedOperationException("Shouldn't be calling the getter");
        }

        public void setRegisteredMember(Member registeredMember) {
            throw new UnsupportedOperationException("Shouldn't be calling the setter with: " + registeredMember);
        }

        public Topic getTopic() {
            this.topicAccessorWasCalled = true;
            return favouriteTopic;
        }

        public void setTopic(Topic favouriteTopic) {
            this.topicAccessorWasCalled = true;
            this.favouriteTopic = favouriteTopic;
        }

        @Relationship(type = "COMMENT")
        public Comment getReadOnlyComment() {
            return this.readOnlyComment;
        }

        public String getPropertyMethodsIgnored() {
            return propertyMethodsIgnored;
        }

        public void setPropertyMethodsIgnored(String propertyMethodsIgnored) {
            this.propertyMethodsIgnored = propertyMethodsIgnored;
        }

        @JsonIgnore //we've used @JsonIgnore but it could be any other annotation
        public String getDifferentAnnotationOnGetter() {
            return propertyWithDifferentAnnotatedGetter;
        }

        @JsonIgnore
        public void setDifferentAnnotationOnGetter(String propertyWithDifferentAnnotatedGetter) {
            this.propertyWithDifferentAnnotatedGetter = propertyWithDifferentAnnotatedGetter;
        }
    }

}
