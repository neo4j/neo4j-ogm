package org.neo4j.ogm.metadata;

import org.junit.Test;
import org.neo4j.ogm.mapper.domain.canonical.Mappable;
import org.neo4j.ogm.strategy.simple.SimpleFieldDictionary;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SimpleFieldDictionaryTest {

    @Test
    public void testPrimitive() throws Exception {
        Field f = new SimpleFieldDictionary().findField("primitiveInt", 0, new Mappable());
        assertEquals("primitiveInt", f.getName());
    }


    @Test
    public void testPrimitiveArray() throws Exception {
        Field f = new SimpleFieldDictionary().findField("primitiveLongArray", new Long[] { 1L, 2L }, new Mappable());
        assertEquals("primitiveLongArray", f.getName());
    }


    @Test
    public void testObject() throws Exception {
        Field f = new SimpleFieldDictionary().findField("objectString", "Hello World", new Mappable());
        assertEquals("objectString", f.getName());
    }

    @Test
    public void testObjectCollection() throws Exception {
        List<String> stringList = new ArrayList();

        stringList.add("Hello");
        stringList.add("World");

        Field f = new SimpleFieldDictionary().findField("listOfAnything", stringList, new Mappable());
        assertEquals("listOfAnything", f.getName());
    }

    @Test
    public void testObjectArray() throws Exception {
        List<String> stringList = new ArrayList();

        stringList.add("Hello");
        stringList.add("World");

        Field f = new SimpleFieldDictionary().findField("objectStringArray", stringList, new Mappable());
        assertEquals("objectStringArray", f.getName());
    }
}
