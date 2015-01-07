package org.neo4j.ogm.unit.metadata;

import org.junit.Test;
import org.neo4j.ogm.metadata.RelationshipUtils;

import static org.junit.Assert.assertEquals;

public class RelationshipUtilsTest {

    @Test
    public void testFieldNameInferenceFromRelationshipType() {
        expect("writesPolicy", RelationshipUtils.inferFieldName("WRITES_POLICY"));
    }

    @Test
    public void testGetterNameInference() {
        expect("getWritesPolicy", RelationshipUtils.inferGetterName("WRITES_POLICY"));
    }

    @Test
    public void testSetterNameInference() {
        expect("setWritesPolicy", RelationshipUtils.inferSetterName("WRITES_POLICY"));
    }

    //
    @Test
    public void testRelationshipTypeInferenceFromFieldName() {
        expect("WRITES_POLICY", RelationshipUtils.inferRelationshipType("writesPolicy"));
    }

    @Test
    public void testRelationshipTypeInferenceFromGetterName() {
        expect("WRITES_POLICY", RelationshipUtils.inferRelationshipType("getWritesPolicy"));
    }

    @Test
    public void testRelationshipTypeInferenceSetterName() {
        expect("WRITES_POLICY", RelationshipUtils.inferRelationshipType("setWritesPolicy"));
    }

    //
    @Test
    public void testSimpleFieldNameInferenceFromRelationshipType() {
        expect("policy", RelationshipUtils.inferFieldName("POLICY"));
    }

    @Test
    public void testSimpleGetterNameInference() {
        expect("getPolicy", RelationshipUtils.inferGetterName("POLICY"));
    }

    @Test
    public void testSimpleSetterNameInference() {
        expect("setPolicy", RelationshipUtils.inferSetterName("POLICY"));
    }

    //
    @Test
    public void testSimpleRelationshipTypeInferenceFromFieldName() {
        expect("POLICY", RelationshipUtils.inferRelationshipType("policy"));
    }

    @Test
    public void testSimpleRelationshipTypeInferenceFromGetterName() {
        expect("POLICY", RelationshipUtils.inferRelationshipType("getPolicy"));
    }

    @Test
    public void testSimpleRelationshipTypeInferenceSetterName() {
        expect("POLICY", RelationshipUtils.inferRelationshipType("setPolicy"));
    }

    private void expect(String expected, String actual) {
        assertEquals(expected, actual);
    }
}
