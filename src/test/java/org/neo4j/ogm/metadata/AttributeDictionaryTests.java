package org.neo4j.ogm.metadata;

import org.junit.Test;
import org.neo4j.ogm.mapper.domain.bike.Bike;
import org.neo4j.ogm.mapper.domain.bike.Saddle;
import org.neo4j.ogm.mapper.domain.education.Student;
import org.neo4j.ogm.metadata.dictionary.AttributeDictionary;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Encapsulates tests that should be satisfied for all {@link AttributeDictionary} implementations.
 */
public abstract class AttributeDictionaryTests {

    protected abstract AttributeDictionary provideAttributeDictionaryToTest();

    @Test
    public void shouldRetriveAttributesOfParticularClassThatRepresentOtherCompositeEntities() {
        Set<String> saddleAttributeNames = provideAttributeDictionaryToTest().lookUpCompositeEntityAttributesFromType(Saddle.class);

        assertNotNull(saddleAttributeNames);
        assertTrue(saddleAttributeNames.isEmpty());

        Set<String> bikeAttributeNames = provideAttributeDictionaryToTest().lookUpCompositeEntityAttributesFromType(Bike.class);
        assertEquals(new HashSet<>(Arrays.asList("frame", "saddle", "wheels")), bikeAttributeNames);
    }

    @Test
    public void shouldRetrieveAttributesOfParticularClassThatRepresentScalarValues() {
        Set<String> attributeNames = provideAttributeDictionaryToTest().lookUpValueAttributesFromType(Student.class);
        assertNotNull(attributeNames);
        assertEquals(2, attributeNames.size());
        assertTrue(attributeNames.contains("name"));
        assertTrue(attributeNames.contains("id"));
    }

    @Test
    public void shouldResolveRelationshipTypeCorrespondingToAttributeName() {
        assertEquals("HAS_FRAME", provideAttributeDictionaryToTest().lookUpRelationshipTypeForAttribute("frame"));

        // XXX: bit quirky, this one, but it is the simple strategy after all so is it just a pill we have to swallow?
        assertEquals("HAS_WHEELS", provideAttributeDictionaryToTest().lookUpRelationshipTypeForAttribute("wheels"));
    }

    @Test
    public void shouldRetrievePropertyNameCorrespondingToNamedAttribute() {
        assertEquals("material", provideAttributeDictionaryToTest().lookUpPropertyNameForAttribute("material"));
    }

}
