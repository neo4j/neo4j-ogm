package org.neo4j.ogm.metadata;

import org.junit.Test;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;

import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class MetaDataTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.mapper.domain.forum");

    @Test
    public void testClassInfo() {
        assertEquals("org.neo4j.ogm.mapper.domain.forum.Topic", metaData.classInfo("Topic").name());
    }

    @Test
    public void testAnnotatedClassInfo() {
        assertEquals("org.neo4j.ogm.mapper.domain.forum.Member", metaData.classInfo("User").name());
        assertEquals("org.neo4j.ogm.mapper.domain.forum.BronzeMembership", metaData.classInfo("Bronze").name());
    }

    @Test
    public void testIdentity() {
        ClassInfo classInfo = metaData.classInfo("Login");
        assertEquals("id", metaData.identityField(classInfo).getName());
        classInfo = metaData.classInfo("Bronze");
        assertEquals("id", metaData.identityField(classInfo).getName());
    }

    @Test
    public void testAnnotatedIdentity() {
        ClassInfo classInfo = metaData.classInfo("Topic");
        assertEquals("topicId", metaData.identityField(classInfo).getName());
    }

    @Test
    public void testPropertyFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Bronze");
        Collection<FieldInfo> fieldInfos = metaData.propertyFields(classInfo);

        int i = 1;
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("fees")) i--;
        }
        assertEquals(0, i);
    }

    @Test
    public void testAnnotatedPropertyFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Bronze");
        Collection<FieldInfo> fieldInfos = metaData.propertyFields(classInfo);

        FieldInfo fieldInfo = fieldInfos.iterator().next();
        assertEquals("annualFees", fieldInfo.property());

    }

    @Test
    public void testPropertyFieldIsNotARelationshipField() {

        ClassInfo classInfo = metaData.classInfo("Bronze");
        Collection<FieldInfo> fieldInfos = metaData.propertyFields(classInfo);

        FieldInfo fieldInfo = fieldInfos.iterator().next();
        assertNull(fieldInfo.relationship());

    }

    @Test
    public void testRelationshipFieldInfo() {
        ClassInfo classInfo = metaData.classInfo("Member");
        Collection<FieldInfo> fieldInfos = metaData.relationshipFields(classInfo);

        int i = 5;
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("renewalDate")) i--;
            if (fieldInfo.getName().equals("activityList")) i--;
            if (fieldInfo.getName().equals("followees")) i--;
            if (fieldInfo.getName().equals("memberShip")) i--;
            if (fieldInfo.getName().equals("followers")) i--;
        }
        assertEquals(0, i);

    }

    @Test
    public void testAnnotatedRelationshipFieldInfo() {
        ClassInfo classInfo = metaData.classInfo("Topic");
        Collection<FieldInfo> fieldInfos = metaData.relationshipFields(classInfo);

        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("posts")) assertEquals("HAS_POSTS", fieldInfo.relationship());
        }
    }

    @Test
    public void testRelationshipFieldIsNotAPropertyField() {

        ClassInfo classInfo = metaData.classInfo("Bronze");
        Collection<FieldInfo> fieldInfos = metaData.relationshipFields(classInfo);

        FieldInfo fieldInfo = fieldInfos.iterator().next();
        assertNull(fieldInfo.property());

    }

    @Test
    public void testNamedPropertyField() {
        ClassInfo classInfo = metaData.classInfo("Gold");
        FieldInfo fieldInfo = metaData.propertyField(classInfo, "annualFees");
        assertEquals("fees", fieldInfo.getName());
    }

    @Test
    public void testNamedRelationshipField() {
        ClassInfo classInfo = metaData.classInfo("Topic");
        FieldInfo fieldInfo = metaData.relationshipField(classInfo, "HAS_POSTS");
        assertEquals("posts", fieldInfo.getName());
    }


}
