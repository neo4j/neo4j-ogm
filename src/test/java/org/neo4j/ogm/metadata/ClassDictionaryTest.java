package org.neo4j.ogm.metadata;

import org.junit.Test;
import org.neo4j.ogm.mapper.domain.bike.Bike;
import org.neo4j.ogm.strategy.simple.SimpleClassDictionary;

import static junit.framework.Assert.assertEquals;

public class ClassDictionaryTest {

    @Test
    public void testFQNamespaceOfBikeDomain() {

        // in this implementation, classloader must know about the bike class.

        Class root = Bike.class;

        // is the ClassDictionary interface on taxa what we need? Seems like it should be <String>
        SimpleClassDictionary scd = new SimpleClassDictionary();

        assertEquals("org.neo4j.ogm.mapper.domain.bike.Bike", scd.getFQNs("Bike").get(0));
        assertEquals("org.neo4j.ogm.mapper.domain.bike.Wheel", scd.getFQNs("Wheel").get(0));
        assertEquals("org.neo4j.ogm.mapper.domain.bike.Frame", scd.getFQNs("Frame").get(0));
        assertEquals("org.neo4j.ogm.mapper.domain.bike.Saddle", scd.getFQNs("Saddle").get(0));


    }
}
