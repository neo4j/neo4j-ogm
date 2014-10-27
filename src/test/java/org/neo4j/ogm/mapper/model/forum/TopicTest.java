package org.neo4j.ogm.mapper.model.forum;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.metadata.dictionary.ClassDictionary;
import org.neo4j.ogm.metadata.info.DomainInfo;
import org.neo4j.ogm.strategy.annotated.AnnotatedClassDictionary;

import static org.junit.Assert.assertEquals;

public class TopicTest {

    private ClassDictionary dictionary;

    @Before
    public void setUp() {
        dictionary = new AnnotatedClassDictionary(new DomainInfo("org.neo4j.ogm.mapper.domain.forum"));
    }

    @Test
    public void testAnnotatedRelationshipAccessViaField() {
        assertEquals("org.neo4j.ogm.mapper.domain.forum.Topic", dictionary.determineLeafClass("Topic"));
    }

//    @Test
//    public void testAnnotatedRelationshipAccessViaGetter() {
//        assertEquals("org.neo4j.ogm.mapper.domain.forum.Topic", dictionary.determineLeafClass("Topic"));
//
//    }
//
//    @Test
//    public void testAnnotatedRelationshipAccessViaSetter() {
//        assertEquals("org.neo4j.ogm.mapper.domain.forum.Topic", dictionary.determineLeafClass("Topic"));
//    }

}
