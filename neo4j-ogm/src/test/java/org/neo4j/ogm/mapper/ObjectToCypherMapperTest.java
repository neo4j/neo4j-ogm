package org.neo4j.ogm.mapper;

import org.junit.*;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.mapper.domain.education.Course;
import org.neo4j.ogm.mapper.domain.education.Student;
import org.neo4j.ogm.metadata.MetaData;
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

    @BeforeClass
    public static void setUpTestDatabase() {
        graphDatabase = new TestGraphDatabaseFactory().newImpermanentDatabase();
        executionEngine = new ExecutionEngine(graphDatabase);
    }

    @AfterClass
    public static void shutDownDatabase() {
        graphDatabase.shutdown();
    }

    @Before
    public void setUpMapper() {
        this.mapper = new MetaDataDrivenObjectToCypherMapper(new MetaData("org.neo4j.ogm.mapper.domain.education"));
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

        List<String> cypher = this.mapper.mapToCypher(newStudent);
        executeStatementsAndAssertSameGraph(cypher, "CREATE (:Student:DomainObject {name:\"Gary\"})");
    }

    @Test
    public void shouldProduceCypherForUpdatingExistingSimpleObject() {
        ExecutionResult executionResult = executionEngine.execute("CREATE (s:Student {name:'Sheila Smythe'}) RETURN id(s) AS id");
        Long existingNodeId = Long.valueOf(executionResult.iterator().next().get("id").toString());

        Student newStudent = new Student();
        newStudent.setId(existingNodeId);
        newStudent.setName("Sheila Smythe-Jones");

        List<String> cypher = this.mapper.mapToCypher(newStudent);
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
        List<String> cypher = this.mapper.mapToCypher(existingCourse);

        executeStatementsAndAssertSameGraph(cypher, "CREATE (c:Course {name:'BSc Computer Science'}), " +
                "(x:Student:DomainObject {name:'Gianfranco'}), (y:Student:DomainObject {name:'Lakshmipathy'}) " +
                "WITH c, x, y MERGE (c)-[:STUDENTS]->(x) MERGE (c)-[:STUDENTS]->(y)");
    }

    private void executeStatementsAndAssertSameGraph(List<String> cypher, String sameGraphCypher) {
        assertNotNull("The resultant cypher shouldn't be null", cypher);
        assertFalse("The resultant cypher statements shouldn't be empty", cypher.isEmpty());

        for (String query : cypher) {
            executionEngine.execute(query);
        }
        assertSameGraph(graphDatabase, sameGraphCypher);
    }

}
