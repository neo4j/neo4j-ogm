package org.neo4j.ogm.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.neo4j.ogm.mapper.domain.bike.Bike;
import org.neo4j.ogm.mapper.domain.bike.Saddle;
import org.neo4j.ogm.mapper.domain.canonical.Mappable;
import org.neo4j.ogm.mapper.domain.education.Student;
import org.neo4j.ogm.strategy.simple.SimpleFieldDictionary;

public class SimpleFieldDictionaryTest {

    private final SimpleFieldDictionary fieldDictionary = new SimpleFieldDictionary();

    @Test
    public void testPrimitive() throws Exception {
        Field f = fieldDictionary.findField("primitiveInt", 0, new Mappable());
        assertEquals("primitiveInt", f.getName());
    }


    @Test
    public void testPrimitiveArray() throws Exception {
        Field f = fieldDictionary.findField("primitiveLongArray", new Long[] { 1L, 2L }, new Mappable());
        assertEquals("primitiveLongArray", f.getName());
    }


    @Test
    public void testObject() throws Exception {
        Field f = fieldDictionary.findField("objectString", "Hello World", new Mappable());
        assertEquals("objectString", f.getName());
    }

    @Test
    public void testObjectCollection() throws Exception {
        List<String> stringList = new ArrayList<>();

        stringList.add("Hello");
        stringList.add("World");

        Field f = fieldDictionary.findField("listOfAnything", stringList, new Mappable());
        assertEquals("listOfAnything", f.getName());
    }

    @Test
    public void testObjectArray() throws Exception {
        List<String> stringList = new ArrayList<>();

        stringList.add("Hello");
        stringList.add("World");

        Field f = fieldDictionary.findField("objectStringArray", stringList, new Mappable());
        assertEquals("objectStringArray", f.getName());
    }

    @Test
    public void shouldRetriveAttributesOfParticularClassThatRepresentOtherCompositeEntities() {
        Set<String> saddleAttributeNames = fieldDictionary.lookUpCompositeEntityAttributesFromType(Saddle.class);
        assertNotNull(saddleAttributeNames);
        assertTrue(saddleAttributeNames.isEmpty());

        Set<String> bikeAttributeNames = fieldDictionary.lookUpCompositeEntityAttributesFromType(Bike.class);
        assertEquals(new HashSet<>(Arrays.asList("frame", "saddle", "wheels")), bikeAttributeNames);
    }

    @Test
    public void shouldRetrieveAttributesOfParticularClassThatRepresentScalarValues() {
        Set<String> attributeNames = fieldDictionary.lookUpValueAttributesFromType(Student.class);
        assertNotNull(attributeNames);
        assertEquals(2, attributeNames.size());
        assertTrue(attributeNames.contains("name"));
        assertTrue(attributeNames.contains("id"));
    }

    @Test
    public void shouldResolveRelationshipTypeCorrespondingToAttributeName() {
        assertEquals("HAS_FRAME", fieldDictionary.lookUpRelationshipTypeForAttribute("frame"));

        // XXX: bit quirky, this one, but it is the simple strategy after all so is it just a pill we have to swallow?
        assertEquals("HAS_WHEELS", fieldDictionary.lookUpRelationshipTypeForAttribute("wheels"));
    }

    @Test
    public void shouldRetrievePropertyNameCorrespondingToNamedAttribute() {
        assertEquals("material", fieldDictionary.lookUpPropertyNameForAttribute("material"));
    }

}
