package org.neo4j.ogm.unit.mapper;

import org.junit.*;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.domain.education.Course;
import org.neo4j.ogm.domain.education.School;
import org.neo4j.ogm.domain.education.Student;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.mapper.MetaDataDrivenObjectToCypherMapper;
import org.neo4j.ogm.mapper.ObjectToCypherMapper;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.mapper.cypher.ParameterisedQuery;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static org.junit.Assert.*;

public class ObjectToCypherMapperTest {

    private ObjectToCypherMapper mapper;

    private static GraphDatabaseService graphDatabase;
    private static ExecutionEngine executionEngine;
    private static MetaData mappingMetadata;

    @BeforeClass
    public static void setUpTestDatabase() {
        graphDatabase = new TestGraphDatabaseFactory().newImpermanentDatabase();
        executionEngine = new ExecutionEngine(graphDatabase);
        mappingMetadata = new MetaData("org.neo4j.ogm.domain.education");
    }

    @AfterClass
    public static void shutDownDatabase() {
        graphDatabase.shutdown();
    }

    @Before
    public void setUpMapper() {
        this.mapper = new MetaDataDrivenObjectToCypherMapper(mappingMetadata);
    }

    @After
    public void cleanGraph() {
        executionEngine.execute("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnAttemptToMapNullObjectToCypherQuery() {
        this.mapper.mapToCypher(null);
    }

    @Test
    public void shouldProduceCypherForCreatingNewSimpleObject() {
        Student newStudent = new Student();
        newStudent.setName("Gary");

        assertNull(newStudent.getId());

        List<ParameterisedQuery> cypher = this.mapper.mapToCypher(newStudent);
        executeStatementsAndAssertSameGraph(cypher, "CREATE (:Student:DomainObject {name:\"Gary\"})");
    }

    @Test
    public void shouldProduceCypherForUpdatingExistingSimpleObject() {
        ExecutionResult executionResult = executionEngine.execute("CREATE (s:Student {name:'Sheila Smythe'}) RETURN id(s) AS id");
        Long existingNodeId = Long.valueOf(executionResult.iterator().next().get("id").toString());

        Student newStudent = new Student();
        newStudent.setId(existingNodeId);
        newStudent.setName("Sheila Smythe-Jones");

        List<ParameterisedQuery> cypher = this.mapper.mapToCypher(newStudent);
        executeStatementsAndAssertSameGraph(cypher, "CREATE (:Student:DomainObject {name:'Sheila Smythe-Jones'})");
    }

    @Test
    public void shouldProduceCypherToAddNewNodeIntoSmallExistingGraph() {
        // set up one student on a course to begin with and add a new student to it
        ExecutionResult executionResult = executionEngine.execute(
                "CREATE (c:Course {name:'BSc Computer Science'})-[:STUDENTS]->(s:Student:DomainObject {name:'Gianfranco'}) " +
                "RETURN id(s) AS student_id, id(c) AS course_id");
        Map<String, Object> resultSetRow = executionResult.iterator().next();
        Long studentId = Long.valueOf(resultSetRow.get("student_id").toString());
        Long courseId = Long.valueOf(resultSetRow.get("course_id").toString());

        Student persistentStudent = new Student();
        persistentStudent.setId(studentId);
        persistentStudent.setName("Gianfranco");
        Student transientStudent = new Student();
        transientStudent.setName("Lakshmipathy");
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setName("BSc Computer Science");
        existingCourse.setStudents(Arrays.asList(transientStudent, persistentStudent));

        // XXX: NB: currently using a dodgy relationship type because of simple strategy read/write inconsistency
        List<ParameterisedQuery> cypher = this.mapper.mapToCypher(existingCourse);

        executeStatementsAndAssertSameGraph(cypher, "CREATE (c:Course {name:'BSc Computer Science'}), " +
                "(x:Student:DomainObject {name:'Gianfranco'}), (y:Student:DomainObject {name:'Lakshmipathy'}) " +
                "WITH c, x, y MERGE (c)-[:STUDENTS]->(x) MERGE (c)-[:STUDENTS]->(y)");
    }

    @Test
    public void shouldNotGetIntoAnInfiniteLoopWhenSavingObjectsThatReferenceEachOther() {
        Teacher missJones = new Teacher();
        missJones.setName("Miss Jones");
        Teacher mrWhite = new Teacher();
        mrWhite.setName("Mr White");
        School school = new School();
        school.setTeachers(Arrays.asList(missJones, mrWhite));

        List<ParameterisedQuery> cypher = this.mapper.mapToCypher(school);
        executeStatementsAndAssertSameGraph(cypher, "CREATE (j:Teacher {name:'Miss Jones'}), (w:Teacher {name:'Mr White'})," +
                " (s:School:DomainObject), (s)-[:TEACHERS]->(j), (s)-[:TEACHERS]->(w)");
    }

    @Test
    public void shouldCorrectlyPersistObjectGraphsSeveralLevelsDeep() {
        Student sheila = new Student();
        sheila.setName("Sheila Smythe");
        Student gary = new Student();
        gary.setName("Gary Jones");
        Student winston = new Student();
        winston.setName("Winston Charles");

        Course physics = new Course();
        physics.setName("GCSE Physics");
        physics.setStudents(Arrays.asList(gary, sheila));
        Course maths = new Course();
        maths.setName("A-Level Mathematics");
        maths.setStudents(Arrays.asList(sheila, winston));

        Teacher teacher = new Teacher();
        teacher.setName("Mrs Kapoor");
        teacher.setCourses(Arrays.asList(physics, maths));

        List<ParameterisedQuery> cypher = this.mapper.mapToCypher(teacher);
        executeStatementsAndAssertSameGraph(cypher, "CREATE (t:Teacher {name:'Mrs Kapoor'}), "
                + "(p:Course {name:'GCSE Physics'}), (m:Course {name:'A-Level Mathematics'}), "
                + "(s:Student:DomainObject {name:'Sheila Smythe'}), "
                + "(g:Student:DomainObject {name:'Gary Jones'}), "
                + "(w:Student:DomainObject {name:'Winston Charles'}), "
                + "(t)-[:COURSES]->(p)-[:STUDENTS]->(s), (t)-[:COURSES]->(m)-[:STUDENTS]->(s), "
                + "(p)-[:STUDENTS]->(g), (m)-[:STUDENTS]->(w)");
    }

    @Test
    public void shouldNotOverwriteExistingObjectPropertiesAndLabelsThatHaveNotBeenLoadedOnUpdate() {
        ExecutionResult result = executionEngine.execute("CREATE (n:Student:DomainObject:Person {student_id:'mr714'}) RETURN id(n) AS id");
        Long id = Long.valueOf(result.iterator().next().get("id").toString());

        Student student = new Student();
        student.setId(id);
        student.setName("Melanie");

        List<ParameterisedQuery> cypherStatements = this.mapper.mapToCypher(student);
        executeStatementsAndAssertSameGraph(cypherStatements,
                "CREATE (:Student:DomainObject:Person {student_id:'mr714',name:'Melanie'})");
    }

    @Test
    public void shouldCorrectlyAddItemToExistingCollection() {
        ExecutionResult result = executionEngine.execute(
                "CREATE (t:Teacher {name:'Mr Gilbert'})-[:COURSES]->(c:Course {name:'A-Level German'}) " +
                "RETURN id(t) AS teacher_id, id(c) AS course_id");
        Map<String, ?> resultRow = result.iterator().next();
        Long teacherId = (Long) resultRow.get("teacher_id");
        Long courseId = (Long) resultRow.get("course_id");

        Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        teacher.setName("Mr Gilbert");
        Course german = new Course();
        german.setId(courseId);
        german.setName("A-Level German");
        Course spanish = new Course();
        spanish.setName("A-Level Spanish");
        teacher.setCourses(Arrays.asList(german, spanish));

        List<ParameterisedQuery> cypher = this.mapper.mapToCypher(teacher);
        executeStatementsAndAssertSameGraph(cypher, "CREATE (t:Teacher {name:'Mr Gilbert'}), "
                + "(g:Course {name:'A-Level German'}), (s:Course {name:'A-Level Spanish'}),"
                + "(t)-[:COURSES]->(g), (t)-[:COURSES]->(s)");
    }

    private void executeStatementsAndAssertSameGraph(List<ParameterisedQuery> cypher, String sameGraphCypher) {
        assertNotNull("The resultant cypher shouldn't be null", cypher);
        assertFalse("The resultant cypher statements shouldn't be empty", cypher.isEmpty());

        for (ParameterisedQuery query : cypher) {
            executionEngine.execute(query.getCypher(), query.getParameterMap());
        }
        assertSameGraph(graphDatabase, sameGraphCypher);
    }

}
