package org.neo4j.ogm.metadata;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.mapper.domain.bike.Bike;
import org.neo4j.ogm.mapper.domain.bike.Saddle;
import org.neo4j.ogm.mapper.domain.canonical.Mappable;
import org.neo4j.ogm.mapper.domain.education.Student;
import org.neo4j.ogm.strategy.simple.SimpleMethodDictionary;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class SimpleMethodDictionaryTest {

    private SimpleMethodDictionary smd;

    @Before
    public void setUp() {
        smd = new SimpleMethodDictionary();
    }

    @Test
    public void testPrimitiveScalar() throws Exception {

        Object param = 0;

        Method m = smd.setter("setPrimitiveInt", param, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setPrimitiveInt(int)", m.toGenericString());

        testInvokable(m, param);
    }

    @Test
    public void testPrimitiveArray() throws Exception {

        Object param = new int[] { 0, 1, 2, 3, 4 };

        Method m = smd.setter("setPrimitiveIntArray", param, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setPrimitiveIntArray(int[])", m.toGenericString());

        testInvokable(m, param);
    }

    @Test
    public void testObjectScalar() throws Exception {

        Object param = new Integer(3);
        Method m = smd.setter("setObjectInteger", param, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setObjectInteger(java.lang.Integer)", m.toGenericString());

        testInvokable(m, param);
    }

    @Test
    public void testObjectArray() throws Exception {

        Object param = new Integer[] { 3, 1, 4, 5, 9 };

        Method m = smd.setter("setObjectIntegerArray", param, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setObjectIntegerArray(java.lang.Integer[])",
                m.toGenericString());

        testInvokable(m, param);
    }

    @Test
    public void testCollectionOfScalar() throws Exception {

        List<String> stringList = new ArrayList<>();

        stringList.add("Hello");
        stringList.add("World");

        Method m = smd.setter("setListOfAnything", stringList, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setListOfAnything(java.util.List<?>)", m.toGenericString());

        testInvokable(m, stringList);
    }

    @Test
    public void testCollectionOfCollection() throws Exception {

        List<List<String>> listOfStringLists = new ArrayList<>();

        listOfStringLists.add(Arrays.asList(new String[] { "hello" }));
        listOfStringLists.add(Arrays.asList(new String[] { "world" }));

        Method m = smd.setter("setListOfAnything", listOfStringLists, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setListOfAnything(java.util.List<?>)", m.toGenericString());

        testInvokable(m, listOfStringLists);
    }

    @Test
    public void testCollectionToStringArray() throws Exception {

        List<String> stringList = new ArrayList<>();

        stringList.add("Hello");
        stringList.add("World");

        Method m = smd.setter("setObjectStringArray", stringList, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setObjectStringArray(java.lang.String[])",
                m.toGenericString());

        // not invokable as is. method access class will cast this correctly however.

    }

    @Test
    public void testCollectionToBoxableTypeArray() throws Exception {

        List<Integer> stringList = new ArrayList<>();

        stringList.add(1);
        stringList.add(2);

        Method m = smd.setter("setObjectIntegerArray", stringList, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setObjectIntegerArray(java.lang.Integer[])",
                m.toGenericString());

        // not invokable as is. method access class will cast this correctly however.

    }

    @Test
    public void testCollectionToPrimitiveArray() throws Exception {

        List<Integer> integerList = new ArrayList<>();

        integerList.add(1);
        integerList.add(2);

        Method m = smd.setter("setPrimitiveIntArray", integerList, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setPrimitiveIntArray(int[])", m.toGenericString());

    }

    // TODO: refactor because these are identical to t'tests for SimpleFieldDictionary
    @Test
    public void shouldRetriveAttributesOfParticularClassThatRepresentOtherCompositeEntities() {
        Set<String> saddleAttributeNames = smd.lookUpCompositeEntityAttributesFromType(Saddle.class);
        assertNotNull(saddleAttributeNames);
        assertTrue(saddleAttributeNames.isEmpty());

        Set<String> bikeAttributeNames = smd.lookUpCompositeEntityAttributesFromType(Bike.class);
        assertEquals(new HashSet<>(Arrays.asList("frame", "saddle", "wheels")), bikeAttributeNames);
    }

    @Test
    public void shouldRetrieveAttributesOfParticularClassThatRepresentScalarValues() {
        Set<String> attributeNames = smd.lookUpValueAttributesFromType(Student.class);
        assertNotNull(attributeNames);
        assertEquals(2, attributeNames.size());
        assertTrue(attributeNames.contains("name"));
        assertTrue(attributeNames.contains("id"));
    }

    @Test
    public void shouldResolveRelationshipTypeCorrespondingToAttributeName() {
        assertEquals("HAS_FRAME", smd.lookUpRelationshipTypeForAttribute("frame"));

        // XXX: bit quirky, this one, but it is the simple strategy after all so is it just a pill we have to swallow?
        assertEquals("HAS_WHEELS", smd.lookUpRelationshipTypeForAttribute("wheels"));
    }

    @Test
    public void shouldRetrievePropertyNameCorrespondingToNamedAttribute() {
        assertEquals("material", smd.lookUpPropertyNameForAttribute("material"));
    }

    private void testInvokable(Method m, Object... params) {
        try {
            m.invoke(new Mappable(), params);
        } catch (Exception e) {
            fail(m.toGenericString() + "invoked, but failed: " + e.getLocalizedMessage());
        }
    }
}
