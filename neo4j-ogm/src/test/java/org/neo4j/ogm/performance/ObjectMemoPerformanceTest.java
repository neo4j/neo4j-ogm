package org.neo4j.ogm.performance;

import org.junit.Test;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.mapper.ObjectMemo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;

import static org.junit.Assert.assertTrue;

public class ObjectMemoPerformanceTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.education");
    private static final ObjectMemo nodeMemo = new ObjectMemo();

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
