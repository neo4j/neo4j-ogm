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
package org.neo4j.ogm.metadata;

import static org.assertj.core.api.Assertions.*;
import static org.junit.rules.ExpectedException.*;
import static org.neo4j.ogm.annotation.Relationship.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.ogm.domain.cineasts.partial.Knows;
import org.neo4j.ogm.domain.cineasts.partial.Rating;
import org.neo4j.ogm.domain.cineasts.partial.Role;
import org.neo4j.ogm.domain.education.Student;
import org.neo4j.ogm.domain.forum.Member;
import org.neo4j.ogm.domain.forum.activity.Activity;
import org.neo4j.ogm.domain.forum.activity.Post;
import org.neo4j.ogm.domain.metadata.LabelsAnnotationWithWrongType;
import org.neo4j.ogm.domain.pizza.Pizza;
import org.neo4j.ogm.exception.core.InvalidPropertyFieldException;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.utils.EntityUtils;

public class ClassInfoTest {

    @Rule
    public final ExpectedException expectedException = none();

    private MetaData metaData;

    @Before
    public void setUp() {
        metaData = new MetaData("org.neo4j.ogm.domain.forum",
            "org.neo4j.ogm.domain.pizza",
            "org.neo4j.ogm.domain.metadata",
            "org.neo4j.ogm.domain.canonical",
            "org.neo4j.ogm.domain.hierarchy.domain",
            "org.neo4j.ogm.domain.cineasts.partial",
            "org.neo4j.ogm.domain.annotations.ids");
    }

    /**
     * The default identity field is a Long type called "id"
     */
    @Test
    public void identityField() {
        ClassInfo classInfo = metaData.classInfo("Login");
        assertThat(classInfo.identityField().getName()).isEqualTo("id");
        classInfo = metaData.classInfo("Bronze");
        assertThat(classInfo.identityField().getName()).isEqualTo("id");
        classInfo = metaData.classInfo("ValidAnnotations$InternalIdWithAnnotation");
        assertThat(classInfo.identityField().getName()).isEqualTo("identifier");
    }

    /**
     * The annotated identity field is a Long type but called whatever you want
     */
    @Test
    public void testAnnotatedIdentity() {
        ClassInfo classInfo = metaData.classInfo("Topic");
        assertThat(classInfo.identityField().getName()).isEqualTo("topicId");
    }

    /**
     * Fields mappable to node properties
     */
    @Test
    public void testPropertyFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Bronze");
        Collection<FieldInfo> fieldInfos = classInfo.propertyFields();

        int count = 1;
        assertThat(fieldInfos.size()).isEqualTo(count);
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("fees")) {
                count--;
            }
        }
        assertThat(count).isEqualTo(0);
    }

    @Test // see GH-506
    public void invalidPropertyFieldsShouldGiveBetterError() {

        this.expectedException.expect(InvalidPropertyFieldException.class);
        this.expectedException.expectMessage("'org.neo4j.ogm.domain.forum.LeadMembership#yearOfRegistration' is not persistable as property but has not been marked as transient.");

        metaData.classInfo("Lead").propertyFields();
    }

    @Test
    public void testIndexFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Login");

        assertThat(classInfo.containsIndexes()).isTrue();

        Collection<FieldInfo> fieldInfos = classInfo.getIndexFields();

        int count = 1;
        assertThat(fieldInfos.size()).isEqualTo(count);
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("userName")) {
                count--;
            }
        }
        assertThat(count).isEqualTo(0);

        FieldInfo userNameField = fieldInfos.iterator().next();

        assertThat(userNameField.isConstraint()).isTrue();
    }

    @Test
    public void testIndexFieldInfoForIdAnnotation() throws Exception {
        ClassInfo classInfo = metaData.classInfo("ValidAnnotations$Basic");

        assertThat(classInfo.containsIndexes()).isTrue();

        Collection<FieldInfo> fieldInfos = classInfo.getIndexFields();

        assertThat(fieldInfos.size()).isEqualTo(1);

        FieldInfo fieldInfo = fieldInfos.iterator().next();
        assertThat(fieldInfo.getName()).isEqualTo("identifier");
        assertThat(fieldInfo.isConstraint()).isTrue();
    }

    /**
     * Node property names available via .property() (annotation)
     */
    @Test
    public void testAnnotatedPropertyFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Bronze");
        Collection<FieldInfo> fieldInfos = classInfo.propertyFields();

        FieldInfo fieldInfo = fieldInfos.iterator().next();
        assertThat(fieldInfo.property()).isEqualTo("annualFees"); // the node property name
        assertThat(fieldInfo.getName()).isEqualTo("fees"); // the field name
    }

    /**
     * A property field cannot be used as a relationship (node entry)
     */
    @Test
    public void testPropertyFieldIsNotARelationshipField() {

        ClassInfo classInfo = metaData.classInfo("Bronze");
        Collection<FieldInfo> fieldInfos = classInfo.propertyFields();

        FieldInfo fieldInfo = fieldInfos.iterator().next();
        assertThat(fieldInfo.relationship()).isNull();
    }

    /**
     * Find all fields that will be mapped as objects at the end of a relationship
     */
    @Test
    public void testRelationshipFieldInfo() {
        ClassInfo classInfo = metaData.classInfo("Member");
        Collection<FieldInfo> fieldInfos = classInfo.relationshipFields();

        int count = 4;
        assertThat(fieldInfos.size()).isEqualTo(count);
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("activityList")) {
                count--;
            }
            if (fieldInfo.getName().equals("followees")) {
                count--;
            }
            if (fieldInfo.getName().equals("memberShip")) {
                count--;
            }
            if (fieldInfo.getName().equals("followers")) {
                count--;
            }
        }
        assertThat(count).isEqualTo(0);
    }

    /**
     * Relationship fields provide relationship name via .relationship()
     */
    @Test
    public void testAnnotatedRelationshipFieldInfo() {
        ClassInfo classInfo = metaData.classInfo("Topic");
        Collection<FieldInfo> fieldInfos = classInfo.relationshipFields();

        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("posts")) {
                assertThat(fieldInfo.relationship()).isEqualTo("HAS_POSTS");
            }
        }
    }

    /**
     * Relationship fields provide relationship name via .relationship()
     */
    @Test
    public void testNonAnnotatedRelationshipFieldInfo() {
        ClassInfo classInfo = metaData.classInfo("Topic");
        Collection<FieldInfo> fieldInfos = classInfo.relationshipFields();

        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("posts")) {
                assertThat(fieldInfo.relationship()).isEqualTo("HAS_POSTS");
            }
        }
    }

    /**
     * Relationship fields are not mappable to node properties
     */
    @Test
    public void testRelationshipFieldIsNotAPropertyField() {

        ClassInfo classInfo = metaData.classInfo("Member");
        Collection<FieldInfo> fieldInfos = classInfo.relationshipFields();

        FieldInfo fieldInfo = fieldInfos.iterator().next();
        assertThat(fieldInfo.property()).isNull();
    }

    /**
     * A property field can be found using its annotated name (node property value)
     */
    @Test
    public void testNamedPropertyField() {
        ClassInfo classInfo = metaData.classInfo("Gold");
        FieldInfo fieldInfo = classInfo.propertyField("annualFees");
        assertThat(fieldInfo.getName()).isEqualTo("fees");
    }

    /**
     * A relationship field can be found using its annotated name (relationship type value)
     */
    @Test
    public void testNamedRelationshipField() {
        ClassInfo classInfo = metaData.classInfo("Topic");
        FieldInfo fieldInfo = classInfo.relationshipField("HAS_POSTS");
        assertThat(fieldInfo.getName()).isEqualTo("posts");
    }

    @Test
    public void testRelationshipGetters() {
        ClassInfo classInfo = metaData.classInfo("User");
        Collection<FieldInfo> relationshipFields = classInfo.relationshipFields();
        int count = 4;
        assertThat(relationshipFields.size()).isEqualTo(count);
        for (FieldInfo relationshipField : relationshipFields) {
            if (relationshipField.getName().equals("activityList")) {
                count--;
            }
            if (relationshipField.getName().equals("followees")) {
                count--;
            }
            if (relationshipField.getName().equals("memberShip")) {
                count--;
            }
            if (relationshipField.getName().equals("followers")) {
                count--;
            }
        }
        assertThat(count).isEqualTo(0);
    }

    /**
     * Can find methods for getting objects which can be represented as node properties in the graph
     */
    @Test
    public void testPropertyGetters() {
        ClassInfo classInfo = metaData.classInfo("User");
        Collection<FieldInfo> propertyFields = classInfo.propertyFields();
        int count = 6;
        assertThat(propertyFields.size()).isEqualTo(count);
        for (FieldInfo propertyField : propertyFields) {
            if (propertyField.getName().equals("renewalDate")) {
                count--;
            }
            if (propertyField.getName().equals("userName")) {
                count--;
            }
            if (propertyField.getName().equals("password")) {
                count--;
            }
            if (propertyField.getName().equals("membershipNumber")) {
                count--;
            }
            if (propertyField.getName().equals("nicknames")) {
                count--;
            }
            if (propertyField.getName().equals("someComputedValue")) {
                count--;
            }
        }
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void testClassInfoIsFoundForFQN() {
        String fqn = "org.neo4j.ogm.domain.forum.Topic";
        ClassInfo classInfo = metaData.classInfo(fqn);
        assertThat(classInfo.name()).isEqualTo(fqn);
    }

    @Test
    public void testFindDateField() {
        ClassInfo classInfo = metaData.classInfo("Member");
        List<FieldInfo> fieldInfos = classInfo.findFields(Date.class);
        FieldInfo fieldInfo = fieldInfos.iterator().next();
        assertThat(fieldInfo.getName()).isEqualTo("renewalDate");
        assertThat(fieldInfo.hasPropertyConverter()).isTrue();
    }

    @Test
    public void testFindIterableFields() {
        ClassInfo classInfo = metaData.classInfo("User");
        List<FieldInfo> fieldInfos = classInfo.findIterableFields();
        int count = 4;
        assertThat(fieldInfos.size()).isEqualTo(count);
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("followees")) {
                count--;
            }
            if (fieldInfo.getName().equals("followers")) {
                count--;
            }
            if (fieldInfo.getName().equals("activityList")) {
                count--;
            }
            if (fieldInfo.getName().equals("nicknames")) {
                count--;
            }
        }
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void testStaticLabelsForClassInfo() {
        ClassInfo annotatedClassInfo = metaData.classInfo(Member.class.getSimpleName());
        assertThat(annotatedClassInfo.staticLabels()).containsExactly("User", "Login");

        ClassInfo simpleClassInfo = metaData.classInfo("Topic");
        assertThat(simpleClassInfo.staticLabels()).containsOnly("Topic");

        ClassInfo nonAnnotatedClassInfo = new MetaData("org.neo4j.ogm.domain.education")
            .classInfo(Student.class.getSimpleName());
        assertThat(nonAnnotatedClassInfo.staticLabels()).containsExactly("Student", "DomainObject");
    }

    @Test // GH-159
    public void labelFieldOrNull() {
        ClassInfo classInfo = metaData.classInfo(Pizza.class.getSimpleName());
        FieldInfo fieldInfo = classInfo.labelFieldOrNull();
        assertThat(fieldInfo).isNotNull();
        assertThat(fieldInfo.getName()).isEqualTo("labels");
    }

    @Test // GH-159
    public void labelFieldOrNullThrowsMappingExceptionForInvalidType() {
        assertThatThrownBy(() -> {
            LabelsAnnotationWithWrongType entity = new LabelsAnnotationWithWrongType();
            Collection<String> collatedLabels = EntityUtils.labels(entity, metaData);
        }).isInstanceOf(MappingException.class)
            .hasMessage("Field 'labels' in class 'org.neo4j.ogm.domain.metadata.LabelsAnnotationWithWrongType' "
                + "includes the @Labels annotation, however this field is not a type of collection.");
    }

    @Test
    public void testClassInfoForAbstractClassImplementingInterface() {
        assertThat(metaData.classInfo("Membership").interfacesInfo().list().size()).isEqualTo(1);
    }

    @Test
    public void testClassInfoForAbstractClassImplementingInterfaceName() {
        assertThat(metaData.classInfo("Membership").interfacesInfo().list().iterator().next().toString()
            .contains("IMembership")).isTrue();
    }

    @Test
    public void testCollectionFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Member");
        FieldInfo fieldInfo = classInfo.relationshipField("followers");
        assertThat(fieldInfo.isScalar()).isFalse();
    }

    @Test
    public void testArrayFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Member");
        FieldInfo fieldInfo = classInfo.getFieldInfo("nicknames");
        assertThat(fieldInfo.isScalar()).isFalse();
    }

    @Test
    public void testScalarFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Member");
        FieldInfo fieldInfo = classInfo.getFieldInfo("userName");
        assertThat(fieldInfo.isScalar()).isTrue();
    }

    @Test // DATAGRAPH-615
    public void testDefaultLabelOfNodeEntities() {
        ClassInfo classInfo = metaData.classInfo("Forum");
        assertThat(classInfo.neo4jName()).isEqualTo("Forum");
    }

    @Test // DATAGRAPH-615
    public void testDefaultLabelOfRelationshipEntities() {
        ClassInfo classInfo = metaData.classInfo("Nomination");
        assertThat(classInfo.neo4jName()).isEqualTo("NOMINATION");
    }

    @Test // DATAGRAPH-690
    public void testTypeParameterDescriptorForRelationships() {
        ClassInfo classInfo = metaData.classInfo("Topic");
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("HAS_POSTS", Direction.OUTGOING)).isEqualTo(Post.class);
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("HAS_POSTS", Direction.INCOMING)).isNull();
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("DOES_NOT_EXIST", Direction.OUTGOING)).isNull();

        classInfo = metaData.classInfo("Member");
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("HAS_ACTIVITY", Direction.OUTGOING))
            .isEqualTo(Activity.class);
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("FOLLOWERS", Direction.OUTGOING)).isEqualTo(Member.class);
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("FOLLOWEES", Direction.OUTGOING)).isEqualTo(Member.class);
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("HAS_ACTIVITY", Direction.INCOMING)).isNull();
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("FOLLOWERS", Direction.INCOMING)).isNull();
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("FOLLOWEES", Direction.INCOMING)).isNull();

        classInfo = metaData.classInfo("Actor");
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("ACTS_IN", Direction.OUTGOING)).isEqualTo(Role.class);
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("KNOWS", Direction.OUTGOING)).isEqualTo(Knows.class);

        classInfo = metaData.classInfo("Movie");
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("ACTS_IN", Direction.INCOMING)).isEqualTo(Role.class);
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("RATED", Direction.INCOMING)).isEqualTo(Rating.class);
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("ACTS_IN", Direction.OUTGOING)).isNull();
        assertThat(classInfo.getTypeParameterDescriptorForRelationship("ACTS_IN", Direction.OUTGOING)).isNull();

        assertThat(classInfo.getTypeParameterDescriptorForRelationship("HAS", Direction.OUTGOING)).isNull();
    }

    @Test
    public void shouldExcludeStaticInitialisersFromPersistenceMethods() {

        ClassInfo classInfo = metaData.classInfo("SecurityRole");
        Collection<MethodInfo> methodInfos = classInfo.methodsInfo().findMethodInfoBy(m -> true);

        for (MethodInfo methodInfo : methodInfos) {
            assertThat(methodInfo.getName().equals("<clinit>")).isFalse();
        }
    }
}
