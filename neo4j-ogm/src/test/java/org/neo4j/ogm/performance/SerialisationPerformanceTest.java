package org.neo4j.ogm.performance;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.education.Course;
import org.neo4j.ogm.domain.education.Student;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.mapper.MetaDataDrivenObjectToCypherMapper;
import org.neo4j.ogm.mapper.ObjectToCypherMapper;
import org.neo4j.ogm.mapper.cypher.ParameterisedQuery;
import org.neo4j.ogm.metadata.MetaData;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class SerialisationPerformanceTest {

    private ObjectToCypherMapper mapper;
    private static MetaData mappingMetadata;

    @BeforeClass
    public static void setUpTestDatabase() {
        mappingMetadata = new MetaData("org.neo4j.ogm.domain.education");
    }

    @Before
    public void setUpMapper() {
        this.mapper = new MetaDataDrivenObjectToCypherMapper(mappingMetadata);
    }

    @Test
    public void testAverageSerialisationSpeed() {

        int count = 1000;
        int target =1000;          // maximum permitted time (milliseconds) to create the entities;

        // create 3 students
        Student sheila = new Student();
        sheila.setName("Sheila Smythe");
        Student gary = new Student();
        gary.setName("Gary Jones");
        Student winston = new Student();
        winston.setName("Winston Charles");

        // 2 courses with students
        Course physics = new Course();
        physics.setName("GCSE Physics");
        physics.setStudents(Arrays.asList(gary, sheila));
        Course maths = new Course();
        maths.setName("A-Level Mathematics");
        maths.setStudents(Arrays.asList(sheila, winston));

        // and a teacher who teaches both courses
        Teacher teacher = new Teacher();
        teacher.setName("Mrs Kapoor");
        teacher.setCourses(Arrays.asList(physics, maths));

        List<ParameterisedQuery> cypher = null;
        long elapsed = -System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
             cypher = this.mapper.mapToCypher(teacher);
        }
        elapsed += System.currentTimeMillis();

        for (ParameterisedQuery query : cypher) {
            System.out.println(query.getCypher());
            for (Map.Entry<String, Object> entry : query.getParameterMap().entrySet()) {
                System.out.println("property " + entry.getKey() + ":" + entry.getValue());
            }
        }

        System.out.println("Serialised Mrs Kapoor, 2 courses and 3 students " + count + " times in " + elapsed + " milliseconds");

        assertTrue(elapsed < target);
    }


}
