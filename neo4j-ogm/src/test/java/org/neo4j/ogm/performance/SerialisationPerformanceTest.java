package org.neo4j.ogm.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.education.Course;
import org.neo4j.ogm.domain.education.Student;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.ObjectCypherMapper;
import org.neo4j.ogm.mapper.ObjectToCypherMapper;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.session.MappedRelationship;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class SerialisationPerformanceTest {

    private ObjectToCypherMapper mapper;
    private static final ObjectMapper objectMapper= new ObjectMapper();
    private static MetaData mappingMetadata;

    @BeforeClass
    public static void setUpTestDatabase() {
        mappingMetadata = new MetaData("org.neo4j.ogm.domain.education");
    }

    @Before
    public void setUpMapper() {
        this.mapper = new ObjectCypherMapper(mappingMetadata, new ArrayList<MappedRelationship>(), new MappingContext());
    }

    @Test
    public void testAverageSerialisationSpeed() throws Exception {

        int count = 1000;
        int target =2000;          // maximum permitted time (milliseconds) to create the entities;

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

        String statement = null;
        long elapsed = -System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
             statement = objectMapper.writeValueAsString(this.mapper.mapToCypher(teacher)); // create the json representation for Cypher
        }
        elapsed += System.currentTimeMillis();
        System.out.println(statement);

        System.out.println("Serialised Mrs Kapoor, 2 courses and 3 students " + count + " times in " + elapsed + " milliseconds");

        assertTrue(elapsed < target);
    }


}
