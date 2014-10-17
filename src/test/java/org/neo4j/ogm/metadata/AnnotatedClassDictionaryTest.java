package org.neo4j.ogm.metadata;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.strategy.annotated.AnnotatedClassDictionary;
import org.neo4j.ogm.strategy.simple.SimpleClassDictionary;

import static junit.framework.Assert.assertEquals;

public class AnnotatedClassDictionaryTest {

    private SimpleClassDictionary dictionary;

    @Before
    public void setUp() {
        dictionary = new AnnotatedClassDictionary("org.neo4j.ogm.mapper.domain");
    }

    /**
     * Annotated classes map using the annotation name property, (or default class name if not named)
     */
    @Test
    public void testFQNamespaceOfAnnotatedDomain() {
        assertEquals("org.neo4j.ogm.mapper.domain.annotated.UserActivity", dictionary.determineLeafClass("Activity"));
        assertEquals("org.neo4j.ogm.mapper.domain.annotated.GoldUser", dictionary.determineLeafClass("Gold", "Login"));
        assertEquals("org.neo4j.ogm.mapper.domain.annotated.SilverUser", dictionary.determineLeafClass("Silver", "Login"));
        assertEquals("org.neo4j.ogm.mapper.domain.annotated.Admin", dictionary.determineLeafClass("Admin", "Login"));
    }

    /**
     * Non-annotated classes can be still be resolved
     */
    @Test
    public void testFQNamespaceOfBikeDomain() {
        assertEquals("org.neo4j.ogm.mapper.domain.bike.Bike", dictionary.determineLeafClass("Bike"));
        assertEquals("org.neo4j.ogm.mapper.domain.bike.Wheel", dictionary.determineLeafClass("Wheel"));
        assertEquals("org.neo4j.ogm.mapper.domain.bike.Frame", dictionary.determineLeafClass("Frame"));
        assertEquals("org.neo4j.ogm.mapper.domain.bike.Saddle", dictionary.determineLeafClass("Saddle"));
    }

    @Test
    /**
     * Taxa corresponding to interfaces can't be resolved
     */
    public void testInterfaceTaxa() {
        assertEquals(null, dictionary.determineLeafClass("Ruler"));
    }

    @Test
    /**
     * Taxa corresponding to abstract classes can't be resolved
     */
    public void testAbstractClassTaxa() {
        assertEquals(null, dictionary.determineLeafClass("Person", "Monarch"));
    }

    @Test
    /**
     * Taxa not forming a class hierarchy cannot be resolved.
     */
    public void testNoCommonLeafInTaxa() {
        assertEquals(null, dictionary.determineLeafClass("Daughter", "Son"));
    }

    @Test
    /**
     * The ordering of taxa is unimportant.
     */
    public void testMaleHeirIsLeafClassInAnyOrderingOfPrinceSonMaleHeirTaxa() {
        assertEquals("org.neo4j.ogm.mapper.domain.rulers.MaleHeir", dictionary.determineLeafClass("Son", "Prince", "MaleHeir"));
        assertEquals("org.neo4j.ogm.mapper.domain.rulers.MaleHeir", dictionary.determineLeafClass("Son", "MaleHeir", "Prince"));
        assertEquals("org.neo4j.ogm.mapper.domain.rulers.MaleHeir", dictionary.determineLeafClass("Prince", "Son", "MaleHeir"));
        assertEquals("org.neo4j.ogm.mapper.domain.rulers.MaleHeir", dictionary.determineLeafClass("Prince", "MaleHeir", "Son"));
        assertEquals("org.neo4j.ogm.mapper.domain.rulers.MaleHeir", dictionary.determineLeafClass("MaleHeir", "Son", "Prince"));
        assertEquals("org.neo4j.ogm.mapper.domain.rulers.MaleHeir", dictionary.determineLeafClass("MaleHeir", "Prince", "Son"));
    }

    @Test
    /**
     * A subclass will be resolved from a superclass if it is a unique leaf class in the type hierarchy
     */
    public void testLiskovSubstitutionPrinciple() {
        assertEquals("org.neo4j.ogm.mapper.domain.rulers.Princess", dictionary.determineLeafClass("Daughter"));
        assertEquals("org.neo4j.ogm.mapper.domain.rulers.Princess", dictionary.determineLeafClass("Daughter", "Princess"));
    }

    @Test
    /**
     * Taxa not in the domain will be ignored.
     */
    public void testAllNonMemberTaxa() {
        assertEquals(null, dictionary.determineLeafClass("Knight", "Baronet"));
    }

    @Test
    /**
     * Mixing domain and non-domain taxa is permitted.
     */
    public void testNonMemberAndMemberTaxa() {
        assertEquals("org.neo4j.ogm.mapper.domain.rulers.Duke", dictionary.determineLeafClass("Knight", "Baronet", "Duke"));
    }

}
