package org.neo4j.ogm.metadata;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.mapper.domain.canonical.Mappable;
import org.neo4j.ogm.strategy.simple.SimpleMethodDictionary;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.fail;

public class SimpleMethodDictionaryTest {

    private SimpleMethodDictionary smd;

    @Before
    public void setUp() {
        smd = new SimpleMethodDictionary();
    }

    @Test
    public void testPrimitiveScalar() throws Exception {

        Object param = 0;

        Method m = smd.findSetter("setPrimitiveInt", param, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setPrimitiveInt(int)", m.toGenericString());

        testInvokable(m, param);
    }



    @Test
    public void testPrimitiveArray() throws Exception {

        Object param = new int[] { 0, 1, 2, 3, 4 };

        Method m = smd.findSetter("setPrimitiveIntArray", param, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setPrimitiveIntArray(int[])", m.toGenericString());

        testInvokable(m, param);
    }


    @Test
    public void testObjectScalar() throws Exception {

        Object param = new Integer(3);
        Method m = smd.findSetter("setObjectInteger", param, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setObjectInteger(java.lang.Integer)", m.toGenericString());

        testInvokable(m, param);
    }

    @Test
    public void testObjectArray() throws Exception {

        Object param = new Integer[] { 3, 1, 4, 5, 9};

        Method m = smd.findSetter("setObjectIntegerArray", param, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setObjectIntegerArray(java.lang.Integer[])", m.toGenericString());

        testInvokable(m, param);
    }

    @Test
    public void testCollectionOfScalar() throws Exception {

        List<String> stringList = new ArrayList();

        stringList.add("Hello");
        stringList.add("World");

        Method m = smd.findSetter("setListOfAnything", stringList, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setListOfAnything(java.util.List<?>)", m.toGenericString());

        testInvokable(m, stringList);
    }

    @Test
    public void testCollectionOfCollection() throws Exception {

        List<List<String>> listOfStringLists = new ArrayList();


        listOfStringLists.add(Arrays.asList(new String[] {"hello"}));
        listOfStringLists.add(Arrays.asList(new String[] {"world"}));

        Method m = smd.findSetter("setListOfAnything", listOfStringLists, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setListOfAnything(java.util.List<?>)", m.toGenericString());

        testInvokable(m, listOfStringLists);
    }


    @Test
    public void testCollectionToStringArray() throws Exception {

        List<String> stringList = new ArrayList();

        stringList.add("Hello");
        stringList.add("World");

        Method m = smd.findSetter("setObjectStringArray", stringList, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setObjectStringArray(java.lang.String[])", m.toGenericString());

        // not invokable as is. method access class will cast this correctly however.

    }

    @Test
    public void testCollectionToBoxableTypeArray() throws Exception {

        List<Integer> stringList = new ArrayList();

        stringList.add(1);
        stringList.add(2);

        Method m = smd.findSetter("setObjectIntegerArray", stringList, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setObjectIntegerArray(java.lang.Integer[])", m.toGenericString());

        // not invokable as is. method access class will cast this correctly however.

    }


    @Test
    public void testCollectionToPrimitiveArray() throws Exception {

        List<Integer> integerList = new ArrayList();

        integerList.add(1);
        integerList.add(2);

        Method m = smd.findSetter("setPrimitiveIntArray", integerList, new Mappable());

        assertEquals("public void org.neo4j.ogm.mapper.domain.canonical.Mappable.setPrimitiveIntArray(int[])", m.toGenericString());

    }

    private void testInvokable(Method m, Object... params) {
        try {
            m.invoke(new Mappable(), params);
        } catch (Exception e) {
            fail(m.toGenericString() + "invoked, but failed: " + e.getLocalizedMessage());
        }
    }
}
