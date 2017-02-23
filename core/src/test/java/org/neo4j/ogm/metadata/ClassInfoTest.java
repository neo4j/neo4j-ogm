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


package org.neo4j.ogm.metadata;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.domain.cineasts.partial.Knows;
import org.neo4j.ogm.domain.cineasts.partial.Rating;
import org.neo4j.ogm.domain.cineasts.partial.Role;
import org.neo4j.ogm.domain.education.Student;
import org.neo4j.ogm.domain.forum.Member;
import org.neo4j.ogm.domain.forum.activity.Activity;
import org.neo4j.ogm.domain.forum.activity.Post;
import org.neo4j.ogm.domain.pizza.Pizza;
import org.neo4j.ogm.exception.MappingException;
import org.neo4j.ogm.utils.EntityUtils;


public class ClassInfoTest {

    private MetaData metaData;

    @Before
    public void setUp() {
        metaData = new MetaData("org.neo4j.ogm.domain.forum", "org.neo4j.ogm.domain.pizza", "org.neo4j.ogm.metadata", "org.neo4j.ogm.domain.canonical", "org.neo4j.ogm.domain.hierarchy.domain", "org.neo4j.ogm.domain.cineasts.partial");
    }

    /**
     * The default identity field is a Long type called "id"
     */
    @Test
    public void identityField() {
        ClassInfo classInfo = metaData.classInfo("Login");
        assertEquals("id", classInfo.identityField().getName());
        classInfo = metaData.classInfo("Bronze");
        assertEquals("id", classInfo.identityField().getName());
    }

    /**
     * The annotated identity field is a Long type but called whatever you want
     */
    @Test
    public void testAnnotatedIdentity() {
        ClassInfo classInfo = metaData.classInfo("Topic");
        assertEquals("topicId", classInfo.identityField().getName());
    }

    /**
     * Fields mappable to node properties
     */
    @Test
    public void testPropertyFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Bronze");
        Collection<FieldInfo> fieldInfos = classInfo.propertyFields();

        int count = 1;
        assertEquals(count, fieldInfos.size());
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("fees")) count--;
        }
        assertEquals(0, count);
    }

    @Test
    public void testIndexFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Login");

        assertTrue(classInfo.containsIndexes());

        Collection<FieldInfo> fieldInfos = classInfo.getIndexFields();

        int count = 1;
        assertEquals(count, fieldInfos.size());
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("userName")) count--;
        }
        assertEquals(0, count);

        FieldInfo userNameField = fieldInfos.iterator().next();

        assertTrue(userNameField.isConstraint());
    }

    /**
     * Node property names available via .property() (annotation)
     */
    @Test
    public void testAnnotatedPropertyFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Bronze");
        Collection<FieldInfo> fieldInfos = classInfo.propertyFields();

        FieldInfo fieldInfo = fieldInfos.iterator().next();
        assertEquals("annualFees", fieldInfo.property()); // the node property name
        assertEquals("fees", fieldInfo.getName()); // the field name
    }

    /**
     * A property field cannot be used as a relationship (node entry)
     */
    @Test
    public void testPropertyFieldIsNotARelationshipField() {

        ClassInfo classInfo = metaData.classInfo("Bronze");
        Collection<FieldInfo> fieldInfos = classInfo.propertyFields();

        FieldInfo fieldInfo = fieldInfos.iterator().next();
        assertNull(fieldInfo.relationship());
    }

    /**
     * Find all fields that will be mapped as objects at the end of a relationship
     */
    @Test
    public void testRelationshipFieldInfo() {
        ClassInfo classInfo = metaData.classInfo("Member");
        Collection<FieldInfo> fieldInfos = classInfo.relationshipFields();

        int count = 4;
        assertEquals(count, fieldInfos.size());
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("activityList")) count--;
            if (fieldInfo.getName().equals("followees")) count--;
            if (fieldInfo.getName().equals("memberShip")) count--;
            if (fieldInfo.getName().equals("followers")) count--;
        }
        assertEquals(0, count);
    }

    /**
     * Relationship fields provide relationship name via .relationship()
     */
    @Test
    public void testAnnotatedRelationshipFieldInfo() {
        ClassInfo classInfo = metaData.classInfo("Topic");
        Collection<FieldInfo> fieldInfos = classInfo.relationshipFields();

        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("posts")) assertEquals("HAS_POSTS", fieldInfo.relationship());
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
            if (fieldInfo.getName().equals("posts")) assertEquals("HAS_POSTS", fieldInfo.relationship());
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
        assertNull(fieldInfo.property());
    }


    /**
     * A property field can be found using its annotated name (node property value)
     */
    @Test
    public void testNamedPropertyField() {
        ClassInfo classInfo = metaData.classInfo("Gold");
        FieldInfo fieldInfo = classInfo.propertyField("annualFees");
        assertEquals("fees", fieldInfo.getName());
    }

    /**
     * A relationship field can be found using its annotated name (relationship type value)
     */
    @Test
    public void testNamedRelationshipField() {
        ClassInfo classInfo = metaData.classInfo("Topic");
        FieldInfo fieldInfo = classInfo.relationshipField("HAS_POSTS");
        assertEquals("posts", fieldInfo.getName());
    }


    @Test
    public void testRelationshipGetters() {
        ClassInfo classInfo = metaData.classInfo("User");
        Collection<FieldInfo> relationshipFields = classInfo.relationshipFields();
        int count = 4;
        assertEquals(count, relationshipFields.size());
        for (FieldInfo relationshipField : relationshipFields) {
            if (relationshipField.getName().equals("activityList")) count--;
            if (relationshipField.getName().equals("followees")) count--;
            if (relationshipField.getName().equals("memberShip")) count--;
            if (relationshipField.getName().equals("followers")) count--;
        }
        assertEquals(0, count);
    }


    /**
     * Can find methods for getting objects which can be represented as node properties in the graph
     */
    @Test
    public void testPropertyGetters() {
        ClassInfo classInfo = metaData.classInfo("User");
        Collection<FieldInfo> propertyFields = classInfo.propertyFields();
        int count = 5;
        assertEquals(count, propertyFields.size());
        for (FieldInfo propertyField : propertyFields) {
            if (propertyField.getName().equals("renewalDate")) count--;
            if (propertyField.getName().equals("userName")) count--;
            if (propertyField.getName().equals("password")) count--;
            if (propertyField.getName().equals("membershipNumber")) count--;
            if (propertyField.getName().equals("nicknames")) count--;
        }
        assertEquals(0, count);
    }


    @Test
    public void testClassInfoIsFoundForFQN() {
        String fqn = "org.neo4j.ogm.domain.forum.Topic";
        ClassInfo classInfo = metaData.classInfo(fqn);
        assertEquals(fqn, classInfo.name());
    }

    @Test
    public void testFindDateField() {
        ClassInfo classInfo = metaData.classInfo("Member");
        List<FieldInfo> fieldInfos = classInfo.findFields(Date.class);
        FieldInfo fieldInfo = fieldInfos.iterator().next();
        assertEquals("renewalDate", fieldInfo.getName());
        assertTrue(fieldInfo.hasPropertyConverter());
    }

    @Test
    public void testFindIterableFields() {
        ClassInfo classInfo = metaData.classInfo("User");
        List<FieldInfo> fieldInfos = classInfo.findIterableFields();
        int count = 4;
        assertEquals(count, fieldInfos.size());
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("followees")) count--;
            if (fieldInfo.getName().equals("followers")) count--;
            if (fieldInfo.getName().equals("activityList")) count--;
            if (fieldInfo.getName().equals("nicknames")) count--;
        }
        assertEquals(0, count);
    }


    @Test
    public void testStaticLabelsForClassInfo() {
        ClassInfo annotatedClassInfo = metaData.classInfo(Member.class.getSimpleName());
        assertEquals(Arrays.asList("User", "Login"), annotatedClassInfo.staticLabels());

        ClassInfo simpleClassInfo = metaData.classInfo("Topic");
        assertEquals(Arrays.asList("Topic"), simpleClassInfo.staticLabels());

        ClassInfo nonAnnotatedClassInfo = new MetaData("org.neo4j.ogm.domain.education").classInfo(Student.class.getSimpleName());
        assertEquals(Arrays.asList("Student", "DomainObject"), nonAnnotatedClassInfo.staticLabels());
    }

    /**
     * @see issue #159
     */
    @Test
    public void labelFieldOrNull() {
        ClassInfo classInfo = metaData.classInfo(Pizza.class.getSimpleName());
        FieldInfo fieldInfo = classInfo.labelFieldOrNull();
        assertNotNull(fieldInfo);
        assertEquals("labels", fieldInfo.getName());
    }

    /**
     * @see issue #159
     */
    @Test
    public void labelFieldOrNullThrowsMappingExceptionForInvalidType() {
        try {
            LabelsAnnotationWithWrongTye entity = new LabelsAnnotationWithWrongTye();
            Collection<String> collatedLabels = EntityUtils.labels(entity, metaData);
            fail("Should have thrown exception");
        } catch (MappingException e) {
            assertEquals("Field 'labels' in class 'org.neo4j.ogm.metadata.LabelsAnnotationWithWrongTye' includes the @Labels annotation, however this field is not a type of collection.", e.getMessage());
        }
    }


    @Test
    public void testClassInfoForAbstractClassImplementingInterface() {
        assertEquals(1, metaData.classInfo("Membership").interfacesInfo().list().size());
    }

    @Test
    public void testClassInfoForAbstractClassImplementingInterfaceName() {
        assertTrue(metaData.classInfo("Membership").interfacesInfo().list().iterator().next().toString().contains("IMembership"));
    }

    @Test
    public void testCollectionFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Member");
        FieldInfo fieldInfo = classInfo.relationshipField("followers");
        assertFalse(fieldInfo.isScalar());
    }

    @Test
    public void testArrayFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Member");
        FieldInfo fieldInfo = classInfo.fieldsInfo().get("nicknames");
        assertFalse(fieldInfo.isScalar());
    }

    @Test
    public void testScalarFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Member");
        FieldInfo fieldInfo = classInfo.fieldsInfo().get("userName");
        assertTrue(fieldInfo.isScalar());
    }


    /**
     * @see DATAGRAPH-615
     */
    @Test
    public void testDefaultLabelOfNodeEntities() {
        ClassInfo classInfo = metaData.classInfo("Forum");
        assertEquals("Forum", classInfo.neo4jName());
    }

    /**
     * @see DATAGRAPH-615
     */
    @Test
    public void testDefaultLabelOfRelationshipEntities() {
        ClassInfo classInfo = metaData.classInfo("Nomination");
        assertEquals("NOMINATION", classInfo.neo4jName());
    }

    /**
     * @see DATAGRAPH-690
     */
    @Test
    public void testTypeParameterDescriptorForRelationships() {
        ClassInfo classInfo = metaData.classInfo("Topic");
        assertEquals(Post.class, classInfo.getTypeParameterDescriptorForRelationship("HAS_POSTS", Relationship.OUTGOING));
        assertNull(classInfo.getTypeParameterDescriptorForRelationship("HAS_POSTS", Relationship.INCOMING));
        assertNull(classInfo.getTypeParameterDescriptorForRelationship("DOES_NOT_EXIST", Relationship.OUTGOING));

        classInfo = metaData.classInfo("Member");
        assertEquals(Activity.class, classInfo.getTypeParameterDescriptorForRelationship("HAS_ACTIVITY", Relationship.OUTGOING));
        assertEquals(Member.class, classInfo.getTypeParameterDescriptorForRelationship("FOLLOWERS", Relationship.OUTGOING));
        assertEquals(Member.class, classInfo.getTypeParameterDescriptorForRelationship("FOLLOWEES", Relationship.OUTGOING));
        assertNull(classInfo.getTypeParameterDescriptorForRelationship("HAS_ACTIVITY", Relationship.INCOMING));
        assertNull(classInfo.getTypeParameterDescriptorForRelationship("FOLLOWERS", Relationship.INCOMING));
        assertNull(classInfo.getTypeParameterDescriptorForRelationship("FOLLOWEES", Relationship.INCOMING));

        classInfo = metaData.classInfo("Actor");
        assertEquals(Role.class, classInfo.getTypeParameterDescriptorForRelationship("ACTS_IN", Relationship.OUTGOING));
        assertEquals(Knows.class, classInfo.getTypeParameterDescriptorForRelationship("KNOWS", Relationship.OUTGOING));

        classInfo = metaData.classInfo("Movie");
        assertEquals(Role.class, classInfo.getTypeParameterDescriptorForRelationship("ACTS_IN", Relationship.INCOMING));
        assertEquals(Rating.class, classInfo.getTypeParameterDescriptorForRelationship("RATED", Relationship.INCOMING));
        assertNull(classInfo.getTypeParameterDescriptorForRelationship("ACTS_IN", Relationship.OUTGOING));
        assertNull(classInfo.getTypeParameterDescriptorForRelationship("ACTS_IN", Relationship.OUTGOING));

        assertNull(classInfo.getTypeParameterDescriptorForRelationship("HAS", Relationship.OUTGOING));
    }

    @Test
    public void shouldExcludeStaticInitialisersFromPersistenceMethods() {

        ClassInfo classInfo = metaData.classInfo("SecurityRole");
        Collection<MethodInfo> methodInfos = classInfo.methodsInfo().methods();

        for (MethodInfo methodInfo : methodInfos) {
            assertFalse(methodInfo.getName().equals("<clinit>"));
        }
    }
}
