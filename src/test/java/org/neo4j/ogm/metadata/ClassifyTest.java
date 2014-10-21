package org.neo4j.ogm.metadata;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.annotation.Label;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.DomainInfo;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;

public class ClassifyTest {

    private DomainInfo classify;

    @Before
    public void setUp() {
        this.classify = new DomainInfo();
    }

    @Test
    public void testAllClassesFound() {

        classify.scan("org.neo4j.ogm.mapper.domain.rulers");

        assertNotNull(classify.getClass("org.neo4j.ogm.mapper.domain.rulers.Baron"));
        assertNotNull(classify.getClass("org.neo4j.ogm.mapper.domain.rulers.Daughter"));
        assertNotNull(classify.getClass("org.neo4j.ogm.mapper.domain.rulers.Duke"));
        assertNotNull(classify.getClass("org.neo4j.ogm.mapper.domain.rulers.Earl"));
        assertNotNull(classify.getClass("org.neo4j.ogm.mapper.domain.rulers.Emperor"));
        assertNotNull(classify.getClass("org.neo4j.ogm.mapper.domain.rulers.Empress"));
        assertNotNull(classify.getClass("org.neo4j.ogm.mapper.domain.rulers.King"));
        assertNotNull(classify.getClass("org.neo4j.ogm.mapper.domain.rulers.MaleHeir"));
        assertNotNull(classify.getClass("org.neo4j.ogm.mapper.domain.rulers.Marquess"));
        assertNotNull(classify.getClass("org.neo4j.ogm.mapper.domain.rulers.Princess"));
        assertNotNull(classify.getClass("org.neo4j.ogm.mapper.domain.rulers.Queen"));
        assertNotNull(classify.getClass("org.neo4j.ogm.mapper.domain.rulers.Viscount"));

    }

    @Test
    public void testResolveSimpleClassName() {
        classify.scan("org.neo4j.ogm.mapper.domain.rulers");
        assertNotNull(classify.getClassSimpleName("Baron"));
    }

    @Test(expected = MappingException.class)
    public void testCannotResolveSimpleClassNameIfNonUnique() {
        classify.scan("org.neo4j.ogm.mapper.domain.collection", "org.neo4j.ogm.mapper.domain.education");
        classify.getClassSimpleName("DomainObject");
    }

    @Test
    public void testResolveAnnotation() {
        classify.scan("org.neo4j.ogm.mapper.domain.annotated");
        assertNotNull(classify.getNamedClassWithAnnotation("org.neo4j.ogm.annotation.Label", "org.neo4j.ogm.mapper.domain.annotated.UserActivity"));
    }

    @Test
    public void testUserSuppliedAnnotationValue() throws Exception {
        classify.scan("org.neo4j.ogm.mapper.domain.annotated");
        ClassInfo classInfo = classify.getNamedClassWithAnnotation("org.neo4j.ogm.annotation.Label", "org.neo4j.ogm.mapper.domain.annotated.UserActivity");
        Class clazz=Class.forName(classInfo.toString());
        assertTrue(clazz.isAnnotationPresent(Label.class));
        Label label = (Label) clazz.getAnnotation(Label.class);
        assertEquals("Activity", label.name());
    }

    @Test
    public void testDefaultAnnotationValue() throws Exception {
        classify.scan("org.neo4j.ogm.mapper.domain.annotated");
        ClassInfo classInfo = classify.getNamedClassWithAnnotation("org.neo4j.ogm.annotation.Label", "org.neo4j.ogm.mapper.domain.annotated.User");
        Class clazz=Class.forName(classInfo.toString());
        assertTrue(clazz.isAnnotationPresent(Label.class));
        Label label = (Label) clazz.getAnnotation(Label.class);
        assertEquals("", label.name());   // note: the default value ("") will be co-erced by the ogm to the class name.
    }
}
