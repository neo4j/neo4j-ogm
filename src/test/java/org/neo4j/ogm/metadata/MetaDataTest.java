package org.neo4j.ogm.metadata;

import org.junit.Test;
import org.neo4j.ogm.mapper.domain.forum.Member;
import org.neo4j.ogm.mapper.domain.forum.activity.Activity;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.*;


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

        int count = 1;
        assertEquals(count, fieldInfos.size());
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("fees")) count--;
        }
        assertEquals(0, count);
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

        int count = 5;
        assertEquals(count, fieldInfos.size());
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("renewalDate")) count--;
            if (fieldInfo.getName().equals("activityList")) count--;
            if (fieldInfo.getName().equals("followees")) count--;
            if (fieldInfo.getName().equals("memberShip")) count--;
            if (fieldInfo.getName().equals("followers")) count--;
        }
        assertEquals(0, count);

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

        ClassInfo classInfo = metaData.classInfo("Member");
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

    @Test
    public void testIdentityGetter() {
        ClassInfo classInfo = metaData.classInfo("Member"); // can also use 'User' here
        MethodInfo methodInfo = metaData.identityGetter(classInfo);

        assertEquals("getId", methodInfo.getName());
        //assertEquals(null, methodInfo.property());       todo: fixme
        //assertEquals(null, methodInfo.relationship());
    }

    @Test
    public void testIdentitySetter() {
        ClassInfo classInfo = metaData.classInfo("Member"); // can also use 'User' here
        MethodInfo methodInfo = metaData.identitySetter(classInfo);

        assertEquals("setId", methodInfo.getName());
        //assertEquals(null, methodInfo.property());       todo: fixme
        //assertEquals(null, methodInfo.relationship());
    }

    @Test
    public void testAnnotatedIdentityGetter() {
        ClassInfo classInfo = metaData.classInfo("Activity");
        MethodInfo methodInfo = metaData.identityGetter(classInfo);
        assertEquals("getActivityId", methodInfo.getName());
    }

    @Test
    public void testAnnotatedIdentitySetter() {
        ClassInfo classInfo = metaData.classInfo("Activity");
        MethodInfo methodInfo = metaData.identitySetter(classInfo);
        assertEquals("setActivityId", methodInfo.getName());
    }

    @Test
    public void testRelationshipGetters() {
        ClassInfo classInfo = metaData.classInfo("User");
        Collection<MethodInfo> relationshipGetters = metaData.relationshipGetters(classInfo);
        int count = 5;
        assertEquals(count, relationshipGetters.size());
        for (MethodInfo relationshipGetter : relationshipGetters) {
            if (relationshipGetter.getName().equals("getRenewalDate")) count--;
            if (relationshipGetter.getName().equals("getActivityList")) count--;
            if (relationshipGetter.getName().equals("getFollowees")) count--;
            if (relationshipGetter.getName().equals("getMemberShip")) count--;
            if (relationshipGetter.getName().equals("getFollowers")) count--;
        }
        assertEquals(0, count);
    }

    @Test
    public void testRelationshipSetters() {
        ClassInfo classInfo = metaData.classInfo("User");
        Collection<MethodInfo> relationshipSetters = metaData.relationshipSetters(classInfo);
        int count = 5;
        assertEquals(count, relationshipSetters.size());
        for (MethodInfo relationshipSetter : relationshipSetters) {
            if (relationshipSetter.getName().equals("setRenewalDate")) count--;
            if (relationshipSetter.getName().equals("setActivityList")) count--;
            if (relationshipSetter.getName().equals("setFollowees")) count--;
            if (relationshipSetter.getName().equals("setMemberShip")) count--;
            if (relationshipSetter.getName().equals("setFollowers")) count--;
        }
    }

    @Test
    public void testPropertyGetters() {
        ClassInfo classInfo = metaData.classInfo("User");
        Collection<MethodInfo> propertyGetters = metaData.propertyGetters(classInfo);
        int count = 4;
        assertEquals(count, propertyGetters.size());
        for (MethodInfo propertyGetter : propertyGetters) {
            if (propertyGetter.getName().equals("getUserName")) count--;
            if (propertyGetter.getName().equals("getPassword")) count--;
            if (propertyGetter.getName().equals("getMembershipNumber")) count--;
            if (propertyGetter.getName().equals("getNicknames")) count--;
        }
        assertEquals(0, count);
    }

    @Test
    public void testPropertySetters() {
        ClassInfo classInfo = metaData.classInfo("User");
        Collection<MethodInfo> propertySetters = metaData.propertySetters(classInfo);
        int count = 4;
        assertEquals(count, propertySetters.size());
        for (MethodInfo propertySetter : propertySetters) {
            if (propertySetter.getName().equals("setUserName")) count--;
            if (propertySetter.getName().equals("setPassword")) count--;
            if (propertySetter.getName().equals("setMembershipNumber")) count--;
            if (propertySetter.getName().equals("setNicknames")) count--;
        }
        assertEquals(0, count);
    }

    @Test
    public void testNamedPropertyGetter() {
        ClassInfo classInfo = metaData.classInfo("Comment");
        MethodInfo methodInfo = metaData.propertyGetter(classInfo, "remark");
        assertEquals("getComment", methodInfo.getName());
    }

    @Test
    public void testNamedPropertySetter() {
        ClassInfo classInfo = metaData.classInfo("Comment");
        MethodInfo methodInfo = metaData.propertySetter(classInfo, "remark");
        assertEquals("setComment", methodInfo.getName());
    }

    @Test
    public void testNamedRelationshipGetter() {
        ClassInfo classInfo = metaData.classInfo("Member");
        MethodInfo methodInfo = metaData.relationshipGetter(classInfo, "HAS_ACTIVITY");
        assertEquals("getActivityList", methodInfo.getName());
    }

    @Test
    public void testNamedRelationshipSetter() {
        ClassInfo classInfo = metaData.classInfo("Member");
        MethodInfo methodInfo = metaData.relationshipSetter(classInfo, "HAS_ACTIVITY");
        assertEquals("setActivityList", methodInfo.getName());
    }

    @Test
    public void testCanResolveClassHierarchies() {
        ClassInfo classInfo = metaData.resolve("Login", "User");
        assertEquals("org.neo4j.ogm.mapper.domain.forum.Member", classInfo.name());
    }

    @Test
    public void testCannotResolveInconsistentClassHierarchies() {
        ClassInfo classInfo = metaData.resolve("Login", "Topic");
        assertNull(classInfo);
    }

    @Test
    public void testClassInfoIsFoundForFQN() {
        String fqn = "org.neo4j.ogm.mapper.domain.forum.Topic";
        ClassInfo classInfo = metaData.classInfo(fqn);
        assertEquals(fqn, classInfo.name());
    }

    @Test
    public void testCollectionFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Member");
        FieldInfo fieldInfo = metaData.relationshipField(classInfo, "followers");

        assertFalse(metaData.isScalar(classInfo, fieldInfo));

    }

    @Test
    public void testArrayFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Member");
        FieldInfo fieldInfo = classInfo.fieldsInfo().get("nicknames");

        assertFalse(metaData.isScalar(classInfo, fieldInfo));

    }

    @Test
    public void testScalarFieldInfo() {

        ClassInfo classInfo = metaData.classInfo("Member");
        FieldInfo fieldInfo = classInfo.fieldsInfo().get("userName");

        assertTrue(metaData.isScalar(classInfo, fieldInfo));

    }

    @Test
    public void testFindDateSetter() {
        ClassInfo classInfo = metaData.classInfo("Member");
        List<MethodInfo> methodInfos = metaData.findSetters(classInfo, Date.class);
        assertEquals("setRenewalDate", methodInfos.iterator().next().getName());
    }

    @Test
    public void testFindDateField() {
        ClassInfo classInfo = metaData.classInfo("Member");
        List<FieldInfo> fieldInfos = metaData.findFields(classInfo, Date.class);
        assertEquals("renewalDate", fieldInfos.iterator().next().getName());
    }

    @Test
    public void testFindListFields() {
        ClassInfo classInfo = metaData.classInfo("User");
        List<FieldInfo> fieldInfos = metaData.findFields(classInfo, List.class);
        int count = 3;
        assertEquals(count, fieldInfos.size());
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getName().equals("followees")) count--;
            if (fieldInfo.getName().equals("followers")) count--;
            if (fieldInfo.getName().equals("activityList")) count--;
        }
        assertEquals(0, count);
    }

    @Test
    public void testFindIterableFields() {
        ClassInfo classInfo = metaData.classInfo("User");
        List<FieldInfo> fieldInfos = metaData.findIterableFields(classInfo);
        int count = 4;
        assertEquals(count, fieldInfos.size());
        for (FieldInfo fieldInfo : fieldInfos) {
            System.out.println(fieldInfo.getName() +":"+ fieldInfo.getDescriptor());
            if (fieldInfo.getName().equals("followees")) count--;
            if (fieldInfo.getName().equals("followers")) count--;
            if (fieldInfo.getName().equals("activityList")) count--;
            if (fieldInfo.getName().equals("nicknames")) count--;
        }
        assertEquals(0, count);
    }

    @Test
    public void testFindMultipleIterableMethodsWithSameParameterisedType() {
        ClassInfo classInfo = metaData.classInfo("User");
        List<MethodInfo> methodInfos = metaData.findIterableMethods(classInfo, Member.class);
        int count = 2;
        assertEquals(count, methodInfos.size());
        for (MethodInfo methodInfo : methodInfos) {
            if (methodInfo.getName().equals("setFollowees")) count--;
            if (methodInfo.getName().equals("setFollowers")) count--;
        }
        assertEquals(count, 0);
    }

    @Test
    public void testFindIterableMethodWithUniqueParameterisedType() {
        ClassInfo classInfo = metaData.classInfo("User");
        List<MethodInfo> methodInfos = metaData.findIterableMethods(classInfo, Activity.class);
        int count = 1;
        assertEquals(count, methodInfos.size());
        for (MethodInfo methodInfo : methodInfos) {
            if (methodInfo.getName().equals("setActivityList")) count--;
        }
        assertEquals(count, 0);
    }

}
