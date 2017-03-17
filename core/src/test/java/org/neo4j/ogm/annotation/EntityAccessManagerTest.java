/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.forum.ForumTopicLink;
import org.neo4j.ogm.domain.forum.Member;
import org.neo4j.ogm.domain.forum.Topic;
import org.neo4j.ogm.domain.forum.activity.Comment;
import org.neo4j.ogm.domain.forum.activity.Post;
import org.neo4j.ogm.domain.satellites.Program;
import org.neo4j.ogm.domain.satellites.Satellite;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.reflect.*;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;

/**
 * @author Adam George
 * @author Luanne Misquitta
 */
public class EntityAccessManagerTest {

    private DomainInfo domainInfo;

    @Before
    public void setup() {
        domainInfo = DomainInfo.create(
                "org.neo4j.ogm.domain.forum",
                "org.neo4j.ogm.domain.satellites",
                "org.neo4j.ogm.annotation"
        );
    }


    @Test
    public void shouldPreferAnnotatedFieldToPlainMethodWhenFindingPropertyToSet() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        FieldInfo objectAccess = classInfo.getFieldInfo( "testProp");

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

        FieldInfo objectAccess = classInfo.getFieldInfo( "testIgnored");

        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertTrue(objectAccess instanceof FieldInfo);
        assertEquals(String.class, objectAccess.type());
        objectAccess.write(domainObject, "TEST");
        assertEquals("TEST", domainObject.propertyMethodsIgnored);
    }

    @Test
    public void shouldAccessViaFieldCorrespondingToPropertyIfNoAnnotationsOrAccessorMethodsArePresent() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.propertyWithoutAccessorMethods = 9;

        // test writing via field
        FieldInfo writer = classInfo.getFieldInfo( "propertyWithoutAccessorMethods");

        assertNotNull("The resultant writer shouldn't be null", writer);
        writer.write(domainObject, 27);
        assertEquals(27, domainObject.propertyWithoutAccessorMethods);

        // test reading via field
        FieldInfo reader = classInfo.getFieldInfo("propertyWithoutAccessorMethods");
        assertNotNull("The resultant reader shouldn't be null", reader);
        assertEquals(domainObject.propertyWithoutAccessorMethods, reader.readProperty(domainObject));
    }

    @Test
    public void shouldRetrieveObjectAccessForWritingIterableObject() {
        ClassInfo classInfo = this.domainInfo.getClass(Program.class.getName());

        FieldInfo iterableAccess = EntityAccessManager.getIterableField(classInfo, Satellite.class, "satellites", Relationship.OUTGOING);
        assertNotNull("The resultant object accessor shouldn't be null", iterableAccess);
        Program spaceProgramme = new Program();
        iterableAccess.write(spaceProgramme, Arrays.asList(new Satellite()));
        assertNotNull("The satellites list wasn't set correctly", spaceProgramme.getSatellites());
        assertFalse("The satellites list wasn't set correctly", spaceProgramme.getSatellites().isEmpty());
    }

    @Test
    public void shouldPreferAnnotatedFieldToPlainSetterMatchingRelationshipTypeWhenSettingRelationshipObject() {
        // 2nd, try to find a field annotated with with relationship type
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        Member parameter = new Member();

        FieldInfo objectAccess = EntityAccessManager.getRelationalWriter(classInfo, "CONTAINS", Relationship.OUTGOING, parameter);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, parameter);
        assertEquals(domainObject.member, parameter);

        Member otherMember = new Member();
        objectAccess = EntityAccessManager.getRelationalWriter(classInfo, "REGISTERED", Relationship.OUTGOING, otherMember);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, otherMember);
        assertEquals(domainObject.registeredMember, otherMember);
    }


    @Test
    public void shouldPreferFieldBasedOnRelationshipTypeToPlainSetterWithMatchingParameterType() {
        // 4th, try to find a "XYZ" field name where XYZ is derived from the relationship type
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());
        Topic favouriteTopic = new Topic();

        // NB: the setter is called setTopic here, so a relationship type of just "TOPIC" would choose the setter
        FieldInfo objectAccess = EntityAccessManager.getRelationalWriter(classInfo, "FAVOURITE_TOPIC", Relationship.OUTGOING, favouriteTopic);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, favouriteTopic);
        assertEquals(domainObject.favouriteTopic, favouriteTopic);
        assertFalse("The access should be via the field", domainObject.topicAccessorWasCalled);
    }

    @Test
    public void shouldDefaultToFieldThatMatchesTheParameterTypeIfRelationshipTypeCannotBeMatchedAndNoSetterExists() {
        // 6th, try to find a field that shares the same type as the parameter
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());
        Post forumPost = new Post();

        FieldInfo objectAccess = EntityAccessManager.getRelationalWriter(classInfo, "UTTER_RUBBISH", Relationship.OUTGOING, forumPost);
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, forumPost);
        assertEquals(domainObject.postWithoutAccessorMethods, forumPost);
    }


    @Test
    public void shouldPreferAnnotatedFieldToPlainGetterWhenReadingFromAnObject() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.annotatedTestProperty = "more arbitrary text";

        FieldInfo objectAccess = classInfo.getFieldInfo("testProp");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertEquals(domainObject.annotatedTestProperty, objectAccess.readProperty(domainObject));
    }

    @Test
    public void shouldPreferAnnotatedFieldToGetterWhenReadingFromAnObject() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.propertyWithDifferentAnnotatedGetter = "more arbitrary text";
        Collection<FieldInfo> readers = classInfo.propertyFields();

        FieldInfo objectAccess = classInfo.getFieldInfo("differentAnnotationOnGetter");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertEquals(domainObject.propertyWithDifferentAnnotatedGetter, objectAccess.readProperty(domainObject));

        for (FieldInfo reader : readers) {
            if (reader.propertyName().equals("differentAnnotationOnGetter")) {
                assertTrue(reader instanceof FieldInfo);
            }
        }
    }

    @Test
    public void shouldPreferMethodBasedAccessToFieldAccessWhenReadingFromObjectsWithoutAnnotations() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.nonAnnotatedTestProperty = new Double(30.16);

        FieldInfo objectAccess = classInfo.getFieldInfo("nonAnnotatedTestProperty");
        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertEquals(domainObject.nonAnnotatedTestProperty, objectAccess.readProperty(domainObject));
    }

    @Test
    public void shouldPreferAnnotatedFieldToPlainGetterMethodMatchingRelationshipType() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.member = new Member();
        domainObject.registeredMember = new Member();

        FieldInfo reader = EntityAccessManager.getRelationalReader(classInfo, "CONTAINS", Relationship.OUTGOING);
        assertNotNull("The resultant object reader shouldn't be null", reader);
        assertSame(domainObject.member, reader.read(domainObject));
        assertEquals("CONTAINS", reader.relationshipType());

        reader = EntityAccessManager.getRelationalReader(classInfo, "REGISTERED", Relationship.OUTGOING);
        assertNotNull("The resultant object reader shouldn't be null", reader);
        assertSame(domainObject.registeredMember, reader.read(domainObject));
        assertEquals("REGISTERED", reader.relationshipType());

    }

    @Test
    public void shouldReadFromFieldMatchingRelationshipTypeInObjectWithoutAnnotationsOrAccessorMethods() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.postWithoutAccessorMethods = new Post();

        FieldInfo reader = EntityAccessManager.getRelationalReader(classInfo, "POST_WITHOUT_ACCESSOR_METHODS", Relationship.OUTGOING);
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

        FieldInfo idReader = classInfo.identityField();
        assertNotNull("The resultant ID reader shouldn't be null", idReader);
        assertEquals(id, idReader.readProperty(domainObject));
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

        Collection<FieldInfo> relationalAccessors = classInfo.relationshipFields();
        assertNotNull("The resultant list of object accessors shouldn't be null", relationalAccessors);
        assertEquals("An unexpected number of accessors was returned", 7, relationalAccessors.size());

        Map<String, Class<? extends FieldInfo>> expectedRelationalReaders = new HashMap<>();
        expectedRelationalReaders.put("COMMENT", FieldInfo.class);
        expectedRelationalReaders.put("FAVOURITE_TOPIC", FieldInfo.class);
        expectedRelationalReaders.put("CONTAINS", FieldInfo.class);
        expectedRelationalReaders.put("POST_WITHOUT_ACCESSOR_METHODS", FieldInfo.class);
        expectedRelationalReaders.put("NATURAL", FieldInfo.class);
        expectedRelationalReaders.put("ARTIFICIAL", FieldInfo.class);
        expectedRelationalReaders.put("REGISTERED", FieldInfo.class);

        for (FieldInfo objectAccess : relationalAccessors) {
            String relType = objectAccess.relationshipType();
            assertTrue("Relationship type " + relType + " wasn't expected", expectedRelationalReaders.containsKey(relType));
            assertEquals(expectedRelationalReaders.get(relType), objectAccess.getClass());
            assertNotNull(objectAccess.read(domainObject));
        }
    }

    @Test
    public void shouldRetrieveAppropriateObjectAccessToEndNodeAttributeOnRelationshipEntity() {
        ClassInfo relationshipEntityClassInfo = domainInfo.getClass(ForumTopicLink.class.getName());

        FieldInfo endNodeReader = relationshipEntityClassInfo.getEndNodeReader();
        assertNotNull("The resultant end node reader shouldn't be null", endNodeReader);

        ForumTopicLink forumTopicLink = new ForumTopicLink();
        Topic topic = new Topic();
        forumTopicLink.setTopic(topic);
        assertSame("The value wasn't read correctly", topic, endNodeReader.read(forumTopicLink));
    }

    @Test
    public void shouldReturnNullOnAttemptToAccessNonExistentEndNodeAttributeOnRelationshipEntity() {
        ClassInfo classInfoOfNonRelationshipEntity = domainInfo.getClass(Member.class.getName());
        assertNull(classInfoOfNonRelationshipEntity .getEndNodeReader());
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

        FieldInfo objectAccess = EntityAccessManager.getIterableField(classInfo, Satellite.class, "NATURAL", Relationship.OUTGOING);
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

        FieldInfo relationalReader = EntityAccessManager.getIterableField(classInfo, Satellite.class, "NATURAL", Relationship.OUTGOING);
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

        @Relationship(type = "COMMENT")
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

        public String getFullyAnnotatedProperty() {
            this.fullyAnnotatedPropertyAccessorWasCalled = true;
            return fullyAnnotatedProperty;
        }

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
