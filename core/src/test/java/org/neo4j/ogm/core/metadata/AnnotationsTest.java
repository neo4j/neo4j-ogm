package org.neo4j.ogm.core.metadata;

import org.junit.Test;
import org.neo4j.ogm.core.MetaData;

import static org.junit.Assert.assertEquals;

/**
 * @author vince
 */
public class AnnotationsTest {


    @Test
    public void shouldLoadMetaDataWithComplexAnnotations() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.annotations");

        assertEquals("org.neo4j.ogm.domain.annotations.SimpleNode", metaData.classInfo("SimpleNode").name());
        assertEquals("org.neo4j.ogm.domain.annotations.OtherNode", metaData.classInfo("OtherNode").name());

    }
}
