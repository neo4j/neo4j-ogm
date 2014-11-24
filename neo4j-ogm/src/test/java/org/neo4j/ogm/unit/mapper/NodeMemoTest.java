package org.neo4j.ogm.unit.mapper;

import org.junit.Test;
import org.neo4j.ogm.domain.education.School;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.mapper.NodeMemo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NodeMemoTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.education");
    private static final NodeMemo nodeMemo = new NodeMemo();

    @Test
    public void testUnchangedObjectDetected() {

        ClassInfo classInfo = metaData.classInfo(Teacher.class.getName());
        Teacher mrsJones = new Teacher();

        nodeMemo.remember(mrsJones, classInfo);

        mrsJones.setId(115L); // the id field must not be part of the memoised property list

        assertTrue(nodeMemo.remembered(mrsJones, classInfo));

    }

    @Test
    public void testChangedPropertyDetected() {

        ClassInfo classInfo = metaData.classInfo(Teacher.class.getName());
        Teacher teacher = new Teacher("Miss White");

        nodeMemo.remember(teacher, classInfo);

        teacher.setId(115L); // the id field must not be part of the memoised property list
        teacher.setName("Mrs Jones"); // the teacher's name property has changed.

        assertFalse(nodeMemo.remembered(teacher, classInfo));
    }

    @Test
    public void testRelatedObjectChangeDoesNotAffectNodeMemoisation() {

        ClassInfo classInfo = metaData.classInfo(Teacher.class.getName());
        Teacher teacher = new Teacher("Miss White");

        nodeMemo.remember(teacher, classInfo);

        teacher.setId(115L); // the id field must not be part of the memoised property list
        teacher.setSchool(new School("Roedean")); // the teacher's name property has changed.

        assertTrue(nodeMemo.remembered(teacher, classInfo));
    }

    @Test
    public void testCostOfNodeMemoisationIsAcceptable() {

        ClassInfo classInfo = metaData.classInfo(Teacher.class.getName());

        long elapsed = -System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            nodeMemo.remember(new Teacher("mr " + i), classInfo);
        }
        elapsed += System.currentTimeMillis();
        assertTrue(elapsed < 1000);  // at least 100 per second


    }
}
