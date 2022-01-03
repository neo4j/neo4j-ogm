/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.metadata.reflect;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.domain.forum.ForumTopicLink;
import org.neo4j.ogm.domain.forum.Member;
import org.neo4j.ogm.domain.forum.Topic;
import org.neo4j.ogm.domain.forum.activity.Comment;
import org.neo4j.ogm.domain.forum.activity.Post;
import org.neo4j.ogm.domain.satellites.Program;
import org.neo4j.ogm.domain.satellites.Satellite;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.metadata.FieldInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
            "org.neo4j.ogm.metadata.reflect"
        );
    }

    @Test
    public void shouldPreferAnnotatedFieldToPlainMethodWhenFindingPropertyToSet() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        FieldInfo objectAccess = classInfo.getFieldInfo("testProp");

        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();

        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, "TEST");
        assertThat(domainObject.annotatedTestProperty).isEqualTo("TEST");
    }

    /**
     * @see DATAGRAPH-674
     */
    @Test
    public void shouldPreferAnnotatedFieldToMethodNotAnnotatedWithPropertyWhenFindingPropertyToSet() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());
        DummyDomainObject domainObject = new DummyDomainObject();

        FieldInfo objectAccess = classInfo.getFieldInfo("testIgnored");

        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(objectAccess instanceof FieldInfo).isTrue();
        assertThat(objectAccess.type()).isEqualTo(String.class);
        objectAccess.write(domainObject, "TEST");
        assertThat(domainObject.propertyMethodsIgnored).isEqualTo("TEST");
    }

    @Test
    public void shouldAccessViaFieldCorrespondingToPropertyIfNoAnnotationsOrAccessorMethodsArePresent() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.propertyWithoutAccessorMethods = 9;

        // test writing via field
        FieldInfo writer = classInfo.getFieldInfo("propertyWithoutAccessorMethods");

        assertThat(writer).as("The resultant writer shouldn't be null").isNotNull();
        writer.write(domainObject, 27);
        assertThat(domainObject.propertyWithoutAccessorMethods).isEqualTo(27);

        // test reading via field
        FieldInfo reader = classInfo.getFieldInfo("propertyWithoutAccessorMethods");
        assertThat(reader).as("The resultant reader shouldn't be null").isNotNull();
        assertThat(reader.readProperty(domainObject)).isEqualTo(domainObject.propertyWithoutAccessorMethods);
    }

    @Test
    public void shouldRetrieveObjectAccessForWritingIterableObject() {
        ClassInfo classInfo = this.domainInfo.getClass(Program.class.getName());

        FieldInfo iterableAccess = EntityAccessManager
            .getIterableField(classInfo, Satellite.class, "satellites", Relationship.Direction.OUTGOING);
        assertThat(iterableAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        Program spaceProgramme = new Program();
        iterableAccess.write(spaceProgramme, Arrays.asList(new Satellite()));
        assertThat(spaceProgramme.getSatellites()).as("The satellites list wasn't set correctly").isNotNull();
        assertThat(spaceProgramme.getSatellites().isEmpty()).as("The satellites list wasn't set correctly").isFalse();
    }

    @Test
    public void shouldPreferAnnotatedFieldToPlainSetterMatchingRelationshipTypeWhenSettingRelationshipObject() {
        // 2nd, try to find a field annotated with with relationship type
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        Member parameter = new Member();

        FieldInfo objectAccess = EntityAccessManager
            .getRelationalWriter(classInfo, "CONTAINS", Relationship.Direction.OUTGOING, parameter);
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, parameter);
        assertThat(parameter).isEqualTo(domainObject.member);

        Member otherMember = new Member();
        objectAccess = EntityAccessManager
            .getRelationalWriter(classInfo, "REGISTERED", Relationship.Direction.OUTGOING, otherMember);
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, otherMember);
        assertThat(otherMember).isEqualTo(domainObject.registeredMember);
    }

    @Test
    public void shouldPreferFieldBasedOnRelationshipTypeToPlainSetterWithMatchingParameterType() {
        // 4th, try to find a "XYZ" field name where XYZ is derived from the relationship type
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());
        Topic favouriteTopic = new Topic();

        // NB: the setter is called setTopic here, so a relationship type of just "TOPIC" would choose the setter
        FieldInfo objectAccess = EntityAccessManager
            .getRelationalWriter(classInfo, "FAVOURITE_TOPIC", Relationship.Direction.OUTGOING, favouriteTopic);
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, favouriteTopic);
        assertThat(favouriteTopic).isEqualTo(domainObject.favouriteTopic);
        assertThat(domainObject.topicAccessorWasCalled).as("The access should be via the field").isFalse();
    }

    @Test
    public void shouldDefaultToFieldThatMatchesTheParameterTypeIfRelationshipTypeCannotBeMatchedAndNoSetterExists() {
        // 6th, try to find a field that shares the same type as the parameter
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());
        Post forumPost = new Post();

        FieldInfo objectAccess = EntityAccessManager
            .getRelationalWriter(classInfo, "UTTER_RUBBISH", Relationship.Direction.OUTGOING, forumPost);
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, forumPost);
        assertThat(forumPost).isEqualTo(domainObject.postWithoutAccessorMethods);
    }

    @Test
    public void shouldPreferAnnotatedFieldToPlainGetterWhenReadingFromAnObject() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.annotatedTestProperty = "more arbitrary text";

        FieldInfo objectAccess = classInfo.getFieldInfo("testProp");
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(objectAccess.readProperty(domainObject)).isEqualTo(domainObject.annotatedTestProperty);
    }

    @Test
    public void shouldPreferAnnotatedFieldToGetterWhenReadingFromAnObject() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.propertyWithDifferentAnnotatedGetter = "more arbitrary text";
        Collection<FieldInfo> readers = classInfo.propertyFields();

        FieldInfo objectAccess = classInfo.getFieldInfo("differentAnnotationOnGetter");
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(objectAccess.readProperty(domainObject))
            .isEqualTo(domainObject.propertyWithDifferentAnnotatedGetter);

        for (FieldInfo reader : readers) {
            if (reader.propertyName().equals("differentAnnotationOnGetter")) {
                assertThat(reader instanceof FieldInfo).isTrue();
            }
        }
    }

    @Test
    public void shouldPreferMethodBasedAccessToFieldAccessWhenReadingFromObjectsWithoutAnnotations() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.nonAnnotatedTestProperty = 30.16;

        FieldInfo objectAccess = classInfo.getFieldInfo("nonAnnotatedTestProperty");
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(objectAccess.readProperty(domainObject)).isEqualTo(domainObject.nonAnnotatedTestProperty);
    }

    @Test
    public void shouldPreferAnnotatedFieldToPlainGetterMethodMatchingRelationshipType() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.member = new Member();
        domainObject.registeredMember = new Member();

        FieldInfo reader = EntityAccessManager.getRelationalReader(classInfo, "CONTAINS", Relationship.Direction.OUTGOING);
        assertThat(reader).as("The resultant object reader shouldn't be null").isNotNull();
        assertThat(reader.read(domainObject)).isSameAs(domainObject.member);
        assertThat(reader.relationshipType()).isEqualTo("CONTAINS");

        reader = EntityAccessManager.getRelationalReader(classInfo, "REGISTERED", Relationship.Direction.OUTGOING);
        assertThat(reader).as("The resultant object reader shouldn't be null").isNotNull();
        assertThat(reader.read(domainObject)).isSameAs(domainObject.registeredMember);
        assertThat(reader.relationshipType()).isEqualTo("REGISTERED");
    }

    @Test
    public void shouldReadFromFieldMatchingRelationshipTypeInObjectWithoutAnnotationsOrAccessorMethods() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.postWithoutAccessorMethods = new Post();

        FieldInfo reader = EntityAccessManager
            .getRelationalReader(classInfo, "POST_WITHOUT_ACCESSOR_METHODS", Relationship.Direction.OUTGOING);
        assertThat(reader).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(reader.read(domainObject)).isSameAs(domainObject.postWithoutAccessorMethods);
        assertThat(reader.relationshipType()).isEqualTo("POST_WITHOUT_ACCESSOR_METHODS");
    }

    @Test
    public void shouldUseFieldAccessUnconditionallyForReadingIdentityProperty() {
        ClassInfo classInfo = this.domainInfo.getClass(DummyDomainObject.class.getName());

        final long id = 593L;
        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.setId(id);

        FieldInfo idReader = classInfo.identityField();
        assertThat(idReader).as("The resultant ID reader shouldn't be null").isNotNull();
        assertThat(idReader.readProperty(domainObject)).isEqualTo(id);
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
        assertThat(relationalAccessors).as("The resultant list of object accessors shouldn't be null").isNotNull();
        assertThat(relationalAccessors).as("An unexpected number of accessors was returned").hasSize(7);

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
            assertThat(expectedRelationalReaders.containsKey(relType))
                .as("Relationship type " + relType + " wasn't expected").isTrue();
            assertThat(objectAccess.getClass()).isEqualTo(expectedRelationalReaders.get(relType));
            assertThat(objectAccess.read(domainObject)).isNotNull();
        }
    }

    @Test
    public void shouldRetrieveAppropriateObjectAccessToEndNodeAttributeOnRelationshipEntity() {
        ClassInfo relationshipEntityClassInfo = domainInfo.getClass(ForumTopicLink.class.getName());

        FieldInfo endNodeReader = relationshipEntityClassInfo.getEndNodeReader();
        assertThat(endNodeReader).as("The resultant end node reader shouldn't be null").isNotNull();

        ForumTopicLink forumTopicLink = new ForumTopicLink();
        Topic topic = new Topic();
        forumTopicLink.setTopic(topic);
        assertThat(endNodeReader.read(forumTopicLink)).as("The value wasn't read correctly").isSameAs(topic);
    }

    @Test
    public void shouldReturnNullOnAttemptToAccessNonExistentEndNodeAttributeOnRelationshipEntity() {
        ClassInfo classInfoOfNonRelationshipEntity = domainInfo.getClass(Member.class.getName());
        assertThat(classInfoOfNonRelationshipEntity.getEndNodeReader()).isNull();
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

        FieldInfo objectAccess = EntityAccessManager
            .getIterableField(classInfo, Satellite.class, "NATURAL", Relationship.Direction.OUTGOING);
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        DummyDomainObject domainObject = new DummyDomainObject();
        objectAccess.write(domainObject, natural);
        assertThat(domainObject.naturalSatellites).isEqualTo(natural);
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

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, Satellite.class, "NATURAL", Relationship.Direction.OUTGOING);
        assertThat(relationalReader).as("The resultant object accessor shouldn't be null").isNotNull();
        DummyDomainObject domainObject = new DummyDomainObject();
        domainObject.naturalSatellites = natural;
        Object o = relationalReader.read(domainObject);
        assertThat(o).isEqualTo(natural);
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

        public Topic getFavouriteTopic() {
            this.topicAccessorWasCalled = true;
            return favouriteTopic;
        }

        public void setFavouriteTopic(Topic favouriteTopic) {
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
        public String getPropertyWithDifferentAnnotatedGetter() {
            return propertyWithDifferentAnnotatedGetter;
        }

        @JsonIgnore
        public void setPropertyWithDifferentAnnotatedGetter(String propertyWithDifferentAnnotatedGetter) {
            this.propertyWithDifferentAnnotatedGetter = propertyWithDifferentAnnotatedGetter;
        }
    }
}
