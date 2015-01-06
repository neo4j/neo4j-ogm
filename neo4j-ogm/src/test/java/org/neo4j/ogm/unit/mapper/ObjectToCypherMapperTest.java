package org.neo4j.ogm.unit.mapper;

import org.junit.*;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.domain.education.Course;
import org.neo4j.ogm.domain.education.School;
import org.neo4j.ogm.domain.education.Student;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.domain.social.Individual;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.ObjectCypherMapper;
import org.neo4j.ogm.mapper.ObjectToCypherMapper;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.cypher.statement.ParameterisedStatements;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.mapper.MappedRelationship;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.*;

import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static org.junit.Assert.*;

public class ObjectToCypherMapperTest {

    private ObjectToCypherMapper mapper;

    private static GraphDatabaseService graphDatabase;
    private static ExecutionEngine executionEngine;
    private static MetaData mappingMetadata;
    private static MappingContext mappingContext;

    @BeforeClass
    public static void setUpTestDatabase() {
        graphDatabase = new TestGraphDatabaseFactory().newImpermanentDatabase();
        executionEngine = new ExecutionEngine(graphDatabase);
        mappingMetadata = new MetaData("org.neo4j.ogm.domain.education", "org.neo4j.ogm.domain.social");
        mappingContext = new MappingContext(mappingMetadata);

    }

    @AfterClass
    public static void shutDownDatabase() {
        graphDatabase.shutdown();
    }

    @Before
    public void setUpMapper() {
        this.mapper = new ObjectCypherMapper(mappingMetadata, mappingContext);
    }

    @After
    public void cleanGraph() {
        executionEngine.execute("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n");
        mappingContext.clear();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnAttemptToMapNullObjectToCypherQuery() {
        this.mapper.mapToCypher(null);
    }

    @Test
    public void createObjectWithLabelsAndProperties() {

        Student newStudent = new Student("Gary");
        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(newStudent).getStatements());

        assertNull(newStudent.getId());

        expect( "CREATE (_0:`Student`:`DomainObject`{_0_props}) " +
                "RETURN id(_0) AS _0", cypher);

        executeStatementsAndAssertSameGraph(cypher, "CREATE (:Student:DomainObject {name:\"Gary\"})");
    }

    @Test
    public void updateObjectPropertyAndLabel() {

        ExecutionResult executionResult = executionEngine.execute("CREATE (s:Student {name:'Sheila Smythe'}) RETURN id(s) AS id");
        Long sid = Long.valueOf(executionResult.iterator().next().get("id").toString());

        Student sheila = new Student("Sheila Smythe");
        sheila.setId(sid);

        mappingContext.remember(sheila);

        String sheilaNode = var(sid);

        // now update the object's properties locally
        sheila.setName("Sheila Smythe-Jones");

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(sheila).getStatements());

        expect( "MATCH (" + sheilaNode + ") " +
                "WHERE id(" + sheilaNode + ")=" + sid + " " +
                "SET " + sheilaNode + ":`Student`:`DomainObject`, " + sheilaNode + "+={" + sheilaNode + "_props}", cypher);

        executeStatementsAndAssertSameGraph(cypher, "CREATE (s:DomainObject:Student {name:'Sheila Smythe-Jones'})");
    }

    @Test
    public void doNothingIfNothingHasChanged() {

        // fake-load sheila:
        ExecutionResult executionResult = executionEngine.execute("CREATE (s:Student:DomainObject {name:'Sheila Smythe'}) RETURN id(s) AS id");
        Long existingNodeId = Long.valueOf(executionResult.iterator().next().get("id").toString());
        Student sheila = new Student();
        sheila.setId(existingNodeId);
        sheila.setName("Sheila Smythe");
        mappingContext.remember(sheila);


        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(sheila).getStatements());

        expect("", cypher);
    }

    @Test
    public void addObjectToCollection() {

        // fake load one student on a course
        ExecutionResult executionResult = executionEngine.execute(
                "CREATE (c:Course {name:'BSc Computer Science'})-[:STUDENTS]->(s:Student:DomainObject {name:'Gianfranco'}) " +
                "RETURN id(s) AS student_id, id(c) AS course_id");

        Map<String, Object> resultSetRow = executionResult.iterator().next();
        Long studentId = Long.valueOf(resultSetRow.get("student_id").toString());
        Long courseId = Long.valueOf(resultSetRow.get("course_id").toString());

        Student gianFranco = new Student("Gianfranco");
        gianFranco.setId(studentId);
        Course bscComputerScience = new Course("BSc Computer Science");
        bscComputerScience.setId(courseId);

        mappingContext.remember(gianFranco);
        mappingContext.remember(bscComputerScience);
        mappingContext.remember(new MappedRelationship(courseId, "STUDENTS", studentId));

        // create a new student and set both students on the course
        Student lakshmipathy = new Student("Lakshmipathy");

        bscComputerScience.setStudents(Arrays.asList(lakshmipathy, gianFranco));

        // XXX: NB: currently using a dodgy relationship type because of simple strategy read/write relationship naming inconsistency
        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(bscComputerScience).getStatements());

        expect("CREATE (_0:`Student`:`DomainObject`{_0_props}) " +
                "WITH _0 MATCH ($0) WHERE id($0)=0 MERGE ($0)-[:STUDENTS]->(_0) " +
                "RETURN id(_0) AS _0", cypher);

        executeStatementsAndAssertSameGraph(cypher, "CREATE (c:Course {name:'BSc Computer Science'}), " +
                "(x:Student:DomainObject {name:'Gianfranco'}), (y:Student:DomainObject {name:'Lakshmipathy'}) " +
                "WITH c, x, y MERGE (c)-[:STUDENTS]->(x) MERGE (c)-[:STUDENTS]->(y)");
    }

    @Test
    public void persistManyToOneObjectFromSingletonSide() {

        ExecutionResult executionResult = executionEngine.execute(
                "CREATE (s:School:DomainObject {name:'Waller'})-[:TEACHERS]->(t:Teacher {name:'Mary'}) " +
                "RETURN id(s) AS school_id, id(t) AS teacher_id");

        Map<String, Object> resultSetRow = executionResult.iterator().next();
        Long wallerId = Long.valueOf(resultSetRow.get("school_id").toString());
        Long maryId = Long.valueOf(resultSetRow.get("teacher_id").toString());

        School waller = new School("Waller");
        waller.setId(wallerId);

        Teacher mary = new Teacher("Mary");
        mary.setId(maryId);
        mary.setSchool(waller);

        mappingContext.remember(mary);
        mappingContext.remember(waller);
        mappingContext.remember(new MappedRelationship(wallerId, "TEACHERS", maryId));

        // create a new teacher and add him to the school
        Teacher jim = new Teacher("Jim");
        jim.setSchool(waller);

        String schoolNode = var(wallerId);

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(jim).getStatements());

        expect( "CREATE (_0:`Teacher`{_0_props}) " +
                "WITH _0 " +
                "MATCH (" + schoolNode + ") WHERE id(" + schoolNode + ")=" + wallerId + " " +
                "MERGE (" + schoolNode + ")-[:TEACHERS]->(_0) " +
                "WITH " + schoolNode + ",_0 " +
                "MERGE (_0)-[:SCHOOL]->(" + schoolNode + ") " +
                "RETURN id(_0) AS _0", cypher);

        executeStatementsAndAssertSameGraph(cypher,
                "CREATE " +
                "(s:School:DomainObject {name:'Waller'}), " +
                "(m:Teacher {name:'Mary'}), " +
                "(j:Teacher {name:'Jim'}), " +
                "(j)-[:SCHOOL]->(s), " +
                "(s)-[:TEACHERS]->(j), " +
                "(s)-[:TEACHERS]->(m)");
    }

    @Test
    public void shouldNotGetIntoAnInfiniteLoopWhenSavingObjectsThatReferenceEachOther() {

        Teacher missJones = new Teacher("Miss Jones");
        Teacher mrWhite = new Teacher("Mr White");
        School school = new School("Hilly Fields");
        school.setTeachers(Arrays.asList(missJones, mrWhite));

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(school).getStatements());

        // todo optimisation: too many with clauses. only one is necessary, and the merge clauses can be collected together
        expect("CREATE (_0:`School`:`DomainObject`{_0_props}), (_1:`Teacher`{_1_props}), (_2:`Teacher`{_2_props}) " +
                "WITH _0,_1,_2 MERGE (_0)-[:TEACHERS]->(_1) " +
                "WITH _0,_1,_2 MERGE (_0)-[:TEACHERS]->(_2) " +
                "RETURN id(_0) AS _0, id(_1) AS _1, id(_2) AS _2", cypher);

        executeStatementsAndAssertSameGraph(cypher, "CREATE (j:Teacher {name:'Miss Jones'}), (w:Teacher {name:'Mr White'})," +
                " (s:School:DomainObject {name:'Hilly Fields'}), (s)-[:TEACHERS]->(j), (s)-[:TEACHERS]->(w)");
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

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(teacher).getStatements());

        // todo: too many with clauses for merge statements
        // todo: we can build larger merge paths from single-hop merge fragments (but check behaviour of partial paths?)
        expect("CREATE " +
                "(_0:`Teacher`{_0_props}), " +
                "(_1:`Course`{_1_props}), " +
                "(_2:`Student`:`DomainObject`{_2_props}), " +
                "(_3:`Student`:`DomainObject`{_3_props}), " +
                "(_4:`Course`{_4_props}), " +
                "(_5:`Student`:`DomainObject`{_5_props}) " +
                "WITH _0,_1,_2,_3,_4,_5 MERGE (_1)-[:STUDENTS]->(_2) " +
                "WITH _0,_1,_2,_3,_4,_5 MERGE (_1)-[:STUDENTS]->(_3) " +
                "WITH _0,_1,_2,_3,_4,_5 MERGE (_0)-[:COURSES]->(_1) " +
                "WITH _0,_1,_2,_3,_4,_5 MERGE (_4)-[:STUDENTS]->(_3) " +
                "WITH _0,_1,_2,_3,_4,_5 MERGE (_4)-[:STUDENTS]->(_5) " +
                "WITH _0,_1,_2,_3,_4,_5 MERGE (_0)-[:COURSES]->(_4) " +
                "RETURN id(_0) AS _0, id(_1) AS _1, id(_2) AS _2, id(_3) AS _3, id(_4) AS _4, id(_5) AS _5", cypher);

        executeStatementsAndAssertSameGraph(cypher, "CREATE (t:Teacher {name:'Mrs Kapoor'}), "
                + "(p:Course {name:'GCSE Physics'}), (m:Course {name:'A-Level Mathematics'}), "
                + "(s:Student:DomainObject {name:'Sheila Smythe'}), "
                + "(g:Student:DomainObject {name:'Gary Jones'}), "
                + "(w:Student:DomainObject {name:'Winston Charles'}), "
                + "(t)-[:COURSES]->(p)-[:STUDENTS]->(s), (t)-[:COURSES]->(m)-[:STUDENTS]->(s), "
                + "(p)-[:STUDENTS]->(g), (m)-[:STUDENTS]->(w)");
    }

    @Test
    public void shouldCorrectlyRemoveRelationshipWhenItemIsRemovedFromCollection() {
        // simple music course with three students
        ExecutionResult executionResult = executionEngine.execute("CREATE (c:Course {name:'GCSE Music'}), "
                + "(c)-[:STUDENTS]->(x:Student:DomainObject {name:'Xavier'}), "
                + "(c)-[:STUDENTS]->(y:Student:DomainObject {name:'Yvonne'}), "
                + "(c)-[:STUDENTS]->(z:Student:DomainObject {name:'Zack'}) "
                + "RETURN id(c) AS course_id, id(x) AS xid, id(y) AS yid, id(z) AS zid");
        Map<String, ?> results = executionResult.iterator().next();

        Long mid = (Long) results.get("course_id");
        Long xid = (Long) results.get("xid");
        Long yid = (Long) results.get("yid");
        Long zid = (Long) results.get("zid");

        Course music = new Course("GCSE Music");
        music.setId(mid);

        Student xavier = new Student("xavier");
        xavier.setId(xid);

        Student yvonne = new Student("Yvonne");
        yvonne.setId(yid);

        Student zack = new Student("Zack");
        zack.setId(zid);

        mappingContext.remember(new MappedRelationship(mid, "STUDENTS", xid));
        mappingContext.remember(new MappedRelationship(mid, "STUDENTS", yid));
        mappingContext.remember(new MappedRelationship(mid, "STUDENTS", zid));

        mappingContext.remember(xavier);
        mappingContext.remember(yvonne);
        mappingContext.remember(zack);
        mappingContext.remember(music);

        music.setStudents(Arrays.asList(yvonne, xavier, zack));

        // object is now "loaded"
        // now, update the domain model, setting yvonne as the only music student (i.e remove zack and xavier)
        music.setStudents(Arrays.asList(yvonne));

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(music).getStatements());

        executeStatementsAndAssertSameGraph(cypher, "CREATE (:Student:DomainObject {name:'Xavier'}), "
                + "(:Student:DomainObject {name:'Zack'}), "
                + "(:Course {name:'GCSE Music'})-[:STUDENTS]->(:Student:DomainObject {name:'Yvonne'})");
    }

    @Test
    public void shouldCorrectlyRemoveRelationshipWhenItemIsMovedToDifferentCollection() {
        // start with one teacher teachers two courses, each with one student in
        ExecutionResult executionResult = executionEngine.execute(
                "CREATE (t:Teacher {name:'Ms Thompson'}), " +
                "(bs:Course {name:'GNVQ Business Studies'})-[:STUDENTS]->(s:Student:DomainObject {name:'Shivani'}), " +
                "(dt:Course {name:'GCSE Design & Technology'})-[:STUDENTS]->(j:Student:DomainObject {name:'Jeff'}), " +
                "(t)-[:COURSES]->(bs), (t)-[:COURSES]->(dt) " +
                "RETURN id(t) AS teacher_id, id(bs) AS bs_id, id(dt) AS dt_id, id(s) AS s_id");

        Map<String, ?> results = executionResult.iterator().next();

        Long teacherId = (Long) results.get("teacher_id");
        Long businessStudiesCourseId = (Long) results.get("bs_id");
        Long designTechnologyCourseId = (Long) results.get("dt_id");
        Long studentId = (Long) results.get("s_id");

        Course designTech = new Course("GCSE Design & Technology");
        designTech.setId(designTechnologyCourseId);

        Course businessStudies = new Course("GNVQ Business Studies");
        businessStudies.setId(businessStudiesCourseId);

        Teacher msThompson = new Teacher();
        msThompson.setId(teacherId);
        msThompson.setName("Ms Thompson");
        msThompson.setCourses(Arrays.asList(businessStudies, designTech));

        Student shivani = new Student("Shivani");
        shivani.setId(studentId);

        // NB: this simulates the graph not being fully hydrated, so Jeff's enrolment on GCSE D+T should remain untouched
        mappingContext.remember(new MappedRelationship(teacherId, "COURSES", businessStudiesCourseId));
        mappingContext.remember(new MappedRelationship(teacherId, "COURSES", designTechnologyCourseId));
        mappingContext.remember(new MappedRelationship(businessStudiesCourseId, "STUDENTS", studentId));

        mappingContext.remember(msThompson);
        mappingContext.remember(businessStudies);
        mappingContext.remember(designTech);
        mappingContext.remember(shivani);

        // move student from one course to the other
        businessStudies.setStudents(Collections.<Student>emptyList());
        designTech.setStudents(Arrays.asList(shivani));

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(msThompson).getStatements());

        String designTechNode = var(designTech.getId());
        String businessStudiesNode = var(businessStudies.getId());
        String shivaniNode = var(shivani.getId());

        expect( "MATCH (" + designTechNode + ") WHERE id(" + designTechNode + ")=" + designTech.getId() + " " +
                "MATCH (" + shivaniNode + ") WHERE id(" + shivaniNode + ")=" + shivani.getId() + " " +
                "MERGE (" + designTechNode + ")-[:STUDENTS]->(" + shivaniNode + ") " +
                "WITH " + shivaniNode + "," + designTechNode + " " +
                "MATCH (" + businessStudiesNode + ")-[_0:STUDENTS]->(" + shivaniNode + ") WHERE id(" + businessStudiesNode + ")=" + businessStudies.getId() + " DELETE _0", cypher);

        executeStatementsAndAssertSameGraph(cypher, "CREATE (t:Teacher {name:'Ms Thompson'}), " +
                "(bs:Course {name:'GNVQ Business Studies'}), (dt:Course {name:'GCSE Design & Technology'}), " +
                "(dt)-[:STUDENTS]->(j:Student:DomainObject {name:'Jeff'}), " +
                "(dt)-[:STUDENTS]->(s:Student:DomainObject {name:'Shivani'}), " +
                "(t)-[:COURSES]->(bs), (t)-[:COURSES]->(dt)");
    }

    @Test
    public void shouldCorrectlyRemoveRelationshipWhenItemIsDisconnectedFromNonOwningSide() {

        ExecutionResult executionResult = executionEngine.execute("CREATE (s:School:DomainObject), "
                + "(s)-[:TEACHERS]->(j:Teacher {name:'Miss Jones'}), "
                + "(s)-[:TEACHERS]->(w:Teacher {name:'Mr White'}) "
                + "RETURN id(s) AS school_id, id(j) AS jones_id, id(w) AS white_id");

        Map<String, ?> results = executionResult.iterator().next();

        Long schoolId = (Long) results.get("school_id");
        Long whiteId = (Long) results.get("white_id");
        Long jonesId = (Long) results.get("jones_id");

        School hillsRoad = new School("Hills Road Sixth Form College");
        hillsRoad.setId(schoolId);

        Teacher mrWhite = new Teacher("Mr White");
        mrWhite.setId(whiteId);

        Teacher missJones = new Teacher("Miss Jones");
        missJones.setId(jonesId);

        // need to ensure teachers list is mutable
        hillsRoad.setTeachers(new ArrayList<>(Arrays.asList(missJones, mrWhite)));

        mappingContext.remember(new MappedRelationship(schoolId, "TEACHERS", whiteId));
        mappingContext.remember(new MappedRelationship(schoolId, "TEACHERS", jonesId));

        mappingContext.remember(hillsRoad);
        mappingContext.remember(mrWhite);
        mappingContext.remember(missJones);

        // Fire Mr White:
        mrWhite.setSchool(null);

        // at the moment, we can't handle the deleted relationship between school->mrWhite from the mrWhite side
        // so we must persist from the collection (school) side.
        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(hillsRoad).getStatements());

        String hillsRoadNode = var(hillsRoad.getId());
        String mrWhiteNode = var(mrWhite.getId());

        expect( "MATCH (" + hillsRoadNode + ")-[_0:TEACHERS]->(" + mrWhiteNode + ") " +
                "WHERE id(" + hillsRoadNode + ")=" + hillsRoad.getId() + " " +
                "AND id(" + mrWhiteNode + ")=" + mrWhite.getId() + " " +
                "DELETE _0", cypher);

        executeStatementsAndAssertSameGraph(cypher,
                "CREATE (w:Teacher {name:'Mr White'}), " +
                        "(s:School:DomainObject)-[:TEACHERS]->(:Teacher {name:'Miss Jones'})");
    }


    @Test
    public void testVariablePersistenceToDepthZero() {

        Teacher claraOswald = new Teacher();
        Teacher dannyPink = new Teacher();
        School coalHillSchool = new School("Coal Hill");

        coalHillSchool.setTeachers(Arrays.asList(claraOswald, dannyPink));

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(coalHillSchool, 0).getStatements());

        // we don't expect the teachers to be persisted when persisting the school to depth 0
        executeStatementsAndAssertSameGraph(cypher, "CREATE (s:School:DomainObject {name:'Coal Hill'}) RETURN s");
    }

    @Test
    public void shouldGenerateCypherToPersistArraysOfPrimitives() {
        Individual individual = new Individual();
        individual.setName("Jeff");
        individual.setAge(41);
        individual.setPrimitiveIntArray(new int[] {1, 6, 4, 7, 2});

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(individual).getStatements());
        executeStatementsAndAssertSameGraph(cypher, "CREATE (:Individual {name:'Jeff', age:41, primitiveIntArray:[1,6,4,7,2]})");

        ExecutionResult executionResult = executionEngine.execute("MATCH (i:Individual) RETURN i.primitiveIntArray AS ints");
        for (Map<String, Object> result : executionResult) {
            assertEquals("The array wasn't persisted as the correct type", 5, ((int[]) result.get("ints")).length);
        }
    }

    @Test
    public void shouldGenerateCypherToPersistCollectionOfBoxedPrimitivesToArrayOfPrimitives() {
        Individual individual = new Individual();
        individual.setName("Gary");
        individual.setAge(36);
        individual.setFavouriteRadioStations(new Vector<Double>(Arrays.asList(97.4, 105.4, 98.2)));

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(individual).getStatements());
        executeStatementsAndAssertSameGraph(cypher,
                "CREATE (:Individual {name:'Gary', age:36, favouriteRadioStations:[97.4, 105.4, 98.2]})");
    }

    @Test
    public void testVariablePersistenceToDepthOne() {

        School coalHillSchool = new School("Coal Hill");

        Teacher claraOswald = new Teacher("Clara Oswald");
        Teacher dannyPink = new Teacher("Danny Pink");

        Course english = new Course("English");
        Course maths = new Course("Maths");

        // do we need to set both sides?
        coalHillSchool.setTeachers(Arrays.asList(claraOswald, dannyPink));

        // do we need to set both sides?
        claraOswald.setCourses(Arrays.asList(english));
        dannyPink.setCourses(Arrays.asList(maths));

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(coalHillSchool, 1).getStatements());

        // we ONLY expect the school and its teachers to be persisted when persisting the school to depth 1
        executeStatementsAndAssertSameGraph(cypher, "CREATE " +
                "(s:School:DomainObject {name:'Coal Hill'}), " +
                "(c:Teacher {name:'Clara Oswald'}), " +
                "(d:Teacher {name:'Danny Pink'}), " +
                "(s)-[:TEACHERS]->(c), " +
                "(s)-[:TEACHERS]->(d)");

    }

    @Test
    public void testVariablePersistenceToDepthTwo() {

        School coalHillSchool = new School("Coal Hill");

        Teacher claraOswald = new Teacher("Clara Oswald");
        Teacher dannyPink = new Teacher("Danny Pink");

        Course english = new Course("English");
        Course maths = new Course("Maths");

        // do we need to set both sides?
        coalHillSchool.setTeachers(Arrays.asList(claraOswald, dannyPink));

        // do we need to set both sides?
        claraOswald.setCourses(Arrays.asList(english));
        dannyPink.setCourses(Arrays.asList(maths));

        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.mapToCypher(coalHillSchool, 2).getStatements());

        // we expect the school its teachers and the teachers courses to be persisted when persisting the school to depth 2
        executeStatementsAndAssertSameGraph(cypher, "CREATE " +
                "(school:School:DomainObject {name:'Coal Hill'}), " +
                "(clara:Teacher {name:'Clara Oswald'}), " +
                "(danny:Teacher {name:'Danny Pink'}), " +
                "(english:Course {name:'English'}), " +
                "(maths:Course {name:'Maths'}), " +
                "(school)-[:TEACHERS]->(clara), " +
                "(school)-[:TEACHERS]->(danny), " +
                "(danny)-[:COURSES]->(maths), " +
                "(clara)-[:COURSES]->(english)");
    }

    private void executeStatementsAndAssertSameGraph(ParameterisedStatements cypher, String sameGraphCypher) {

        assertNotNull("The resultant cypher statements shouldn't be null", cypher.getStatements());
        assertFalse("The resultant cypher statements shouldn't be empty", cypher.getStatements().isEmpty());

        for (ParameterisedStatement query : cypher.getStatements()) {
            executionEngine.execute(query.getStatement(), query.getParameters());
        }
        assertSameGraph(graphDatabase, sameGraphCypher);
    }

    private void expect(String expected, ParameterisedStatements cypher) {
        assertEquals(expected, cypher.getStatements().get(0).getStatement());

    }

    private String var(Long nodeId) {
        return "$" + nodeId;
    }
}
