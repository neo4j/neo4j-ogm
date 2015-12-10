/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.mapper;

import static org.junit.Assert.*;
import static org.neo4j.ogm.testutil.GraphTestUtils.*;

import java.util.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.compiler.Compiler;
import org.neo4j.ogm.domain.education.Course;
import org.neo4j.ogm.domain.education.School;
import org.neo4j.ogm.domain.education.Student;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.domain.forum.Forum;
import org.neo4j.ogm.domain.forum.ForumTopicLink;
import org.neo4j.ogm.domain.forum.Topic;
import org.neo4j.ogm.domain.policy.Person;
import org.neo4j.ogm.domain.policy.Policy;
import org.neo4j.ogm.domain.social.Individual;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.Statements;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.request.RowStatementFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.IntegrationTestRule;

/**
 * @author Adam George
 * @author Luanne Misquitta
 */
public class EntityGraphMapperTest {


    private EntityMapper mapper;

    private static ExecutionEngine executionEngine;
    private static MetaData mappingMetadata;
    private static MappingContext mappingContext;

    @ClassRule
    public static IntegrationTestRule testServer = new IntegrationTestRule(Components.driver());

    private static SessionFactory sessionFactory;
    private Session session;


    @BeforeClass
    public static void setUpTestDatabase() {

        executionEngine = new ExecutionEngine(getDatabase());
        mappingMetadata = new MetaData(
                "org.neo4j.ogm.domain.education",
                "org.neo4j.ogm.domain.forum",
                "org.neo4j.ogm.domain.social",
                "org.neo4j.ogm.domain.policy");
        mappingContext = new MappingContext(mappingMetadata);

    }

    @Before
    public void setUpMapper() {
        sessionFactory = new SessionFactory("org.neo4j.ogm.domain.policy",
                "org.neo4j.ogm.domain.election", "org.neo4j.ogm.domain.forum",
                "org.neo4j.ogm.domain.education");
        mappingContext.clear();
        this.mapper = new EntityGraphMapper(mappingMetadata, mappingContext);
        session = sessionFactory.openSession(testServer.driver());
        session.purgeDatabase();
    }

    private static GraphDatabaseService getDatabase() {
        return testServer.getGraphDatabaseService();
    }


    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnAttemptToMapNullObjectToCypherQuery() {
        this.mapper.map(null);
    }

    @Test
    public void createObjectWithLabelsAndProperties() {

        Student newStudent = new Student("Gary");

        session.save(newStudent);

        GraphTestUtils.assertSameGraph(getDatabase(), "CREATE (:Student:DomainObject {name:\"Gary\"})");
    }

    @Test
    public void updateObjectPropertyAndLabel() {

        ExecutionResult executionResult = executionEngine.execute("CREATE (s:Student {name:'Sheila Smythe'}) RETURN id(s) AS id");
        Long sid = Long.valueOf(executionResult.iterator().next().get("id").toString());

        Student sheila = session.load(Student.class, sid);

        // now update the object's properties locally
        sheila.setName("Sheila Smythe-Jones");

        session.save(sheila);

        GraphTestUtils.assertSameGraph(getDatabase(), "CREATE (s:DomainObject:Student {name:'Sheila Smythe-Jones'})");
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

        Compiler compiler = this.mapper.map(sheila).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());
        Statements cypher = new Statements(compiler.getAllStatements());

        assertEquals(0, cypher.getStatements().size());
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

        Student gianFranco = session.load(Student.class, studentId);
        Course bscComputerScience = session.load(Course.class, courseId);

        // create a new student and set both students on the course
        Student lakshmipathy = new Student("Lakshmipathy");

        bscComputerScience.setStudents(Arrays.asList(lakshmipathy, gianFranco));

        // XXX: NB: currently using a dodgy relationship type because of simple strategy read/write relationship naming inconsistency
        session.save(bscComputerScience);

        GraphTestUtils.assertSameGraph(getDatabase(), "CREATE (c:Course {name:'BSc Computer Science'}), " +
                "(x:Student:DomainObject {name:'Gianfranco'}), (y:Student:DomainObject {name:'Lakshmipathy'}) " +
                "WITH c, x, y MERGE (c)-[:STUDENTS]->(x) MERGE (c)-[:STUDENTS]->(y)");
    }

    @Test
    public void persistManyToOneObjectFromSingletonSide() {

        ExecutionResult executionResult = executionEngine.execute(
                "CREATE (s:School:DomainObject {name:'Waller'})-[:TEACHERS]->(t:Teacher {name:'Mary'})-[:SCHOOL]->(s) " +
                "RETURN id(s) AS school_id, id(t) AS teacher_id");

        Map<String, Object> resultSetRow = executionResult.iterator().next();
        Long wallerId = Long.valueOf(resultSetRow.get("school_id").toString());
        Long maryId = Long.valueOf(resultSetRow.get("teacher_id").toString());

        School waller = session.load(School.class, wallerId);

        Teacher mary = session.load(Teacher.class, maryId);
        mary.setId(maryId);

        // create a new teacher and add him to the school
        Teacher jim = new Teacher("Jim");
        jim.setSchool(waller);

        // ensure that the domain objects are mutually established by the code
        assertTrue(waller.getTeachers().contains(jim));

        session.save(jim);

        GraphTestUtils.assertSameGraph(getDatabase(),
                "CREATE " +
                "(s:School:DomainObject {name:'Waller'}), " +
                "(m:Teacher {name:'Mary'}), " +
                "(j:Teacher {name:'Jim'}), " +
                "(j)-[:SCHOOL]->(s), " +
                "(m)-[:SCHOOL]->(s), " +
                "(s)-[:TEACHERS]->(j), " +
                "(s)-[:TEACHERS]->(m)");
    }

    @Test
    public void shouldNotGetIntoAnInfiniteLoopWhenSavingObjectsThatReferenceEachOther() {

        Teacher missJones = new Teacher("Miss Jones");
        Teacher mrWhite = new Teacher("Mr White");
        School school = new School("Hilly Fields");
        school.setTeachers(Arrays.asList(missJones, mrWhite));

        session.save(school);

        GraphTestUtils.assertSameGraph(getDatabase(),
                "CREATE (j:Teacher {name:'Miss Jones'}), " +
                        "(w:Teacher {name:'Mr White'}), " +
                        "(s:School:DomainObject {name:'Hilly Fields'}), " +
                        "(s)-[:TEACHERS]->(j)-[:SCHOOL]->(s), " +
                        "(s)-[:TEACHERS]->(w)-[:SCHOOL]->(s)");
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

        session.save(teacher);

        GraphTestUtils.assertSameGraph(getDatabase(), "CREATE (t:Teacher {name:'Mrs Kapoor'}), "
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

        Course music = session.load(Course.class, mid);

        Student xavier = session.load(Student.class, xid);

        Student yvonne = session.load(Student.class, yid);

        Student zack = session.load(Student.class, zid);

        music.setStudents(Arrays.asList(yvonne, xavier, zack));

        // object is now "loaded"
        // now, update the domain model, setting yvonne as the only music student (i.e remove zack and xavier)
        music.setStudents(Arrays.asList(yvonne));

        session.save(music);

        GraphTestUtils.assertSameGraph(getDatabase(), "CREATE (:Student:DomainObject {name:'Xavier'}), "
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

        Course designTech = session.load(Course.class, designTechnologyCourseId);

        Course businessStudies = session.load(Course.class, businessStudiesCourseId);

        Teacher msThompson = session.load(Teacher.class, teacherId);

        Student shivani = session.load(Student.class, studentId);

        // move student from one course to the other
        businessStudies.setStudents(Collections.<Student>emptyList());
        designTech.getStudents().add(shivani);

        session.save(msThompson);

        GraphTestUtils.assertSameGraph(getDatabase(), "CREATE (t:Teacher {name:'Ms Thompson'}), " +
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

        School hillsRoad = session.load(School.class, schoolId);

        Teacher mrWhite = session.load(Teacher.class, whiteId);

        Teacher missJones = session.load(Teacher.class, jonesId);

        // Fire Mr White:
        mrWhite.setSchool(null);

        session.save(hillsRoad);

        GraphTestUtils.assertSameGraph(getDatabase(),
                "CREATE (w:Teacher {name:'Mr White'}), (t:Teacher {name:'Miss Jones'}), (s:School:DomainObject), (s)-[:TEACHERS]->(t), (t)-[:SCHOOL]->(s)");
    }


    @Test
    public void testVariablePersistenceToDepthZero() {

        Teacher claraOswald = new Teacher();
        Teacher dannyPink = new Teacher();
        School coalHillSchool = new School("Coal Hill");

        coalHillSchool.setTeachers(Arrays.asList(claraOswald, dannyPink));

        session.save(coalHillSchool,0);

        // we don't expect the teachers to be persisted when persisting the school to depth 0
        GraphTestUtils.assertSameGraph(getDatabase(), "CREATE (s:School:DomainObject {name:'Coal Hill'}) RETURN s");
    }

    @Test
    public void shouldGenerateCypherToPersistArraysOfPrimitives() {
        sessionFactory = new SessionFactory("org.neo4j.ogm.domain.social");
        session = sessionFactory.openSession(testServer.driver());
        Individual individual = new Individual();
        individual.setName("Jeff");
        individual.setAge(41);
        individual.setBankBalance(1000.50f);
        individual.setPrimitiveIntArray(new int[]{1, 6, 4, 7, 2});

        session.save(individual);

        GraphTestUtils.assertSameGraph(getDatabase(), "CREATE (:Individual {name:'Jeff', age:41, bankBalance: 1000.50, code:0, primitiveIntArray:[1,6,4,7,2]})");

        ExecutionResult executionResult = executionEngine.execute("MATCH (i:Individual) RETURN i.primitiveIntArray AS ints");
        for (Map<String, Object> result : executionResult) {
            assertEquals("The array wasn't persisted as the correct type", 5, ((int[]) result.get("ints")).length);
        }
    }

    @Test
    public void shouldGenerateCypherToPersistByteArray() {
        sessionFactory = new SessionFactory("org.neo4j.ogm.domain.social");
        session = sessionFactory.openSession(testServer.driver());
        Individual individual = new Individual();
        individual.setAge(41);
        individual.setBankBalance(1000.50f);
        individual.setPrimitiveByteArray(new byte[]{1, 2, 3, 4, 5});

        session.save(individual);
        GraphTestUtils.assertSameGraph(getDatabase(), "CREATE (:Individual {age:41, bankBalance: 1000.50, code:0, primitiveByteArray:'AQIDBAU='})");

        ExecutionResult executionResult = executionEngine.execute("MATCH (i:Individual) RETURN i.primitiveByteArray AS bytes");
        for (Map<String, Object> result : executionResult) {
            assertEquals("The array wasn't persisted as the correct type", "AQIDBAU=",result.get("bytes")); //Byte arrays are converted to Base64 Strings
        }
    }

    @Test
    public void shouldGenerateCypherToPersistCollectionOfBoxedPrimitivesToArrayOfPrimitives() {
        sessionFactory = new SessionFactory("org.neo4j.ogm.domain.social");
        session = sessionFactory.openSession(testServer.driver());
        Individual individual = new Individual();
        individual.setName("Gary");
        individual.setAge(36);
        individual.setBankBalance(99.99f);
        individual.setFavouriteRadioStations(new Vector<>(Arrays.asList(97.4, 105.4, 98.2)));

        session.save(individual);
        GraphTestUtils.assertSameGraph(getDatabase(),
                "CREATE (:Individual {name:'Gary', age:36, bankBalance:99.99, code:0, favouriteRadioStations:[97.4, 105.4, 98.2]})");
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

        session.save(coalHillSchool, 1);

        // we ONLY expect the school and its teachers to be persisted when persisting the school to depth 1
        GraphTestUtils.assertSameGraph(getDatabase(), "CREATE " +
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

        session.save(coalHillSchool, 2);

        // we expect the school its teachers and the teachers courses to be persisted when persisting the school to depth 2
        GraphTestUtils.assertSameGraph(getDatabase(), "CREATE " +
                "(school:School:DomainObject {name:'Coal Hill'}), " +
                "(clara:Teacher {name:'Clara Oswald'}), " +
                "(danny:Teacher {name:'Danny Pink'}), " +
                "(english:Course {name:'English'}), " +
                "(maths:Course {name:'Maths'}), " +
                "(school)-[:TEACHERS]->(clara)-[:SCHOOL]->(school), " +
                "(school)-[:TEACHERS]->(danny)-[:SCHOOL]->(school), " +
                "(danny)-[:COURSES]->(maths), " +
                "(clara)-[:COURSES]->(english)");
    }

    @Test
    public void shouldProduceCypherForSavingNewRichRelationshipBetweenNodes() {
        Forum forum = new Forum();
        forum.setName("SDN FAQs");
        Topic topic = new Topic();
        ForumTopicLink link = new ForumTopicLink();
        link.setForum(forum);
        link.setTopic(topic);
        link.setTimestamp(1647209L);
        forum.setTopicsInForum(Arrays.asList(link));

        session.save(forum);
        GraphTestUtils.assertSameGraph(getDatabase(), "CREATE "
                + "(f:Forum {name:'SDN FAQs'})-[:HAS_TOPIC {timestamp:1647209}]->(t:Topic)");
    }

    @Test
    public void shouldProduceCypherForUpdatingExistingRichRelationshipBetweenNodes() {
        ExecutionResult executionResult = executionEngine.execute(
                "CREATE (f:Forum {name:'Spring Data Neo4j'})-[r:HAS_TOPIC {timestamp:20000}]->(t:Topic {inActive:false}) " +
                "RETURN id(f) AS forumId, id(t) AS topicId, ID(r) AS relId");
        Map<String, Object> rs = executionResult.iterator().next();
        Long forumId = (Long) rs.get("forumId");
        Long topicId = (Long) rs.get("topicId");
        Long relationshipId = (Long) rs.get("relId");

        Forum forum = session.load(Forum.class, forumId);
        Topic topic = session.load(Topic.class, topicId);
        ForumTopicLink link = session.load(ForumTopicLink.class, relationshipId);
        link.setTimestamp(327790L);
        forum.setTopicsInForum(Arrays.asList(link));

        session.save(forum);
        GraphTestUtils.assertSameGraph(getDatabase(), "CREATE "
                + "(f:Forum {name:'Spring Data Neo4j'})-[r:HAS_TOPIC {timestamp:327790}]->(t:Topic {inActive:false})");
    }

    @org.junit.Ignore
    @Test
    public void shouldSaveCollectionOfRichRelationships() {
        ExecutionResult executionResult = executionEngine.execute("CREATE "
                + "(f:Forum {name:'SDN 4.x'})-[r:HAS_TOPIC]->(t:Topic) RETURN id(f) AS forumId, ID(r) AS relId, id(t) AS topicId");
        Map<String, Object> resultSet = executionResult.iterator().next();
        Long forumId = (Long) resultSet.get("forumId");
        Long relationshipId = (Long) resultSet.get("relId");
        Long topicId = (Long) resultSet.get("topicId");

        Forum neo4jForum = new Forum();
        neo4jForum.setName("Neo4j Questions");
        Topic neo4jTopicOne = new Topic();
        Topic neo4jTopicTwo = new Topic();

        Forum sdnForum = new Forum();
        sdnForum.setId(forumId);
        sdnForum.setName("SDN 4.x");
        Topic sdnTopic = new Topic();
        sdnTopic.setTopicId(topicId);

        ForumTopicLink firstRelationshipEntity = new ForumTopicLink();
        firstRelationshipEntity.setId(relationshipId);
        firstRelationshipEntity.setForum(sdnForum); // NB these don't set bidirectionally
        firstRelationshipEntity.setTopic(sdnTopic);
        firstRelationshipEntity.setTimestamp(500L);
        ForumTopicLink secondRelationshipEntity = new ForumTopicLink();
        secondRelationshipEntity.setForum(neo4jForum);
        secondRelationshipEntity.setTopic(neo4jTopicTwo);
        secondRelationshipEntity.setTimestamp(750L);
        ForumTopicLink thirdRelationshipEntity = new ForumTopicLink();
        thirdRelationshipEntity.setForum(neo4jForum);
        thirdRelationshipEntity.setTopic(neo4jTopicOne);
        thirdRelationshipEntity.setTimestamp(1000L);
        List<ForumTopicLink> linksToSave = Arrays.asList(firstRelationshipEntity, secondRelationshipEntity, thirdRelationshipEntity);

        // FIXME: currently fails straight away, but do we even support mapping collections in this way?
        Statements cypher = new Statements(this.mapper.map(linksToSave).getCompiler().getAllStatements());
        System.err.println(cypher.getStatements().get(0).getStatement());
        System.err.println(cypher.getStatements().get(0).getParameters());
        executeStatementsAndAssertSameGraph(cypher, "CREATE "
                + "(:Forum {name:'SDN 4.x'})-[:HAS_TOPIC {timestamp:500}]->(x:Topic), "
                + "(f:Forum {name:'Neo4j Questions'})-[:HAS_TOPIC {timestamp:750}]->(y:Topic), "
                + "(f)-[:HAS_TOPIC {timestamp:1000}]->(z:Topic)");
    }


    @Test
    public void testCreateFirstReferenceFromOutgoingSide() {

        Person person1 = new Person("jim");
        Policy policy1 = new Policy("health");

        person1.getWritten().add(policy1);

        session.save(person1);
        GraphTestUtils.assertSameGraph(getDatabase(),
                "CREATE (:Person:DomainObject { name :'jim' })-[:WRITES_POLICY]->(:Policy:DomainObject { name: 'health' })");

    }

    @Test
    public void testCreateFirstReferenceFromIncomingSide() {

        Person person1 = new Person("jim");
        Policy policy1 = new Policy("health");

        policy1.getWriters().add(person1);

        session.save(policy1);
        GraphTestUtils.assertSameGraph(getDatabase(),
                "CREATE (:Person:DomainObject { name :'jim' })-[:WRITES_POLICY]->(:Policy:DomainObject { name: 'health' })");

    }

    @Test
    public void testDeleteExistingReferenceFromOutgoingSide() {

        ExecutionResult executionResult = executionEngine.execute(
                "CREATE (j:Person:DomainObject { name :'jim' })" +
                        "-[r:WRITES_POLICY]->" +
                        "(h:Policy:DomainObject { name: 'health' }) " +
                        "RETURN id(j) AS jid, ID(r) AS rid, id(h) AS hid");

        Map<String, Object> resultSet = executionResult.iterator().next();

        Long jid = (Long) resultSet.get("jid");
        Long hid = (Long) resultSet.get("hid");

        Person person = session.load(Person.class, jid);

        Policy policy = session.load(Policy.class, hid);
        policy.setId(hid);

        // ensure domain model is set up
        policy.getWriters().add(person);
        person.getWritten().add(policy);

        // now remove the relationship from the person side
        person.getWritten().clear();

        session.save(person);
        GraphTestUtils.assertSameGraph(getDatabase(),
                "CREATE (:Person:DomainObject { name :'jim' }) " +
                "CREATE (:Policy:DomainObject { name: 'health' })");


    }

    @Test
    public void testDeleteExistingReferenceFromIncomingSide() {

        ExecutionResult executionResult = executionEngine.execute(
                "CREATE (j:Person:DomainObject { name :'jim' })" +
                        "-[r:WRITES_POLICY]->" +
                        "(h:Policy:DomainObject { name: 'health' }) " +
                        "RETURN id(j) AS jid, ID(r) AS rid, id(h) AS hid");

        Map<String, Object> resultSet = executionResult.iterator().next();

        Long jid = (Long) resultSet.get("jid");
        Long hid = (Long) resultSet.get("hid");

        Person person = session.load(Person.class, jid);

        Policy policy = session.load(Policy.class, hid);

        // ensure domain model is set up
        policy.getWriters().add(person);
        person.getWritten().add(policy);

        // now remove the object from the policy
        policy.getWriters().clear();
        //we have to make sure the domain model is consistent and remove the policy from the person too
        person.getWritten().clear();

        //No relations are
        session.save(policy);
        GraphTestUtils.assertSameGraph(getDatabase(),
                "CREATE (:Person:DomainObject { name :'jim' }) " +
                "CREATE (:Policy:DomainObject { name: 'health' })");


    }

    @Test
    public void testAppendReferenceFromOutgoingSide() {

        ExecutionResult executionResult = executionEngine.execute(
                "CREATE (j:Person:DomainObject { name :'jim' })" +
                "CREATE (h:Policy:DomainObject { name: 'health' }) " +
                "CREATE (i:Policy:DomainObject { name: 'immigration' }) " +
                "CREATE (j)-[r:WRITES_POLICY]->(h) " +
                "RETURN id(j) AS jid, ID(r) AS rid, id(h) AS hid, id(i) as iid");

        Map<String, Object> resultSet = executionResult.iterator().next();

        Long jid = (Long) resultSet.get("jid");
        Long hid = (Long) resultSet.get("hid");
        Long iid = (Long) resultSet.get("iid");

        Person jim = session.load(Person.class, jid);

        Policy health = session.load(Policy.class, hid);

        Policy immigration = session.load(Policy.class, iid);

        // set jim as the writer of the health policy and expect the new relationship to be established
        // alongside the existing one.
        jim.getWritten().add(health);
        jim.getWritten().add(immigration);

        session.save(jim);
        GraphTestUtils.assertSameGraph(getDatabase(),
                "CREATE (j:Person:DomainObject { name :'jim' }) " +
                "CREATE (h:Policy:DomainObject { name: 'health' }) " +
                "CREATE (i:Policy:DomainObject { name: 'immigration' }) " +
                "CREATE (j)-[:WRITES_POLICY]->(h) " +
                "CREATE (j)-[:WRITES_POLICY]->(i) ");


    }

    @Test
    public void testAppendReferenceFromIncomingSide() {

        ExecutionResult executionResult = executionEngine.execute(
                "CREATE (j:Person:DomainObject { name :'jim' })" +
                        "CREATE (h:Policy:DomainObject { name: 'health' }) " +
                        "CREATE (i:Policy:DomainObject { name: 'immigration' }) " +
                        "CREATE (j)-[r:WRITES_POLICY]->(h) " +
                        "RETURN id(j) AS jid, ID(r) AS rid, id(h) AS hid, id(i) as iid");

        Map<String, Object> resultSet = executionResult.iterator().next();

        Long jid = (Long) resultSet.get("jid");
        Long hid = (Long) resultSet.get("hid");
        Long iid = (Long) resultSet.get("iid");

        Person jim = session.load(Person.class, jid);

        Policy health = session.load(Policy.class, hid);

        Policy immigration = session.load(Policy.class, iid);

        // ensure the graph reflects the mapping context
        jim.getWritten().add(health);

        // now add jim as a writer of the immigration policy and expect the existing
        // relationship to be maintained, and a new one created
        immigration.getWriters().add(jim);

        // note that we save the graph to the same depth as we hydrate it.
        session.save(immigration, 2);
       // Statements cypher = new Statements(this.mapper.map(immigration, 2).getCompiler().getAllStatements());
        GraphTestUtils.assertSameGraph(getDatabase(),
                "CREATE (j:Person:DomainObject { name :'jim' }) " +
                        "CREATE (h:Policy:DomainObject { name: 'health' }) " +
                        "CREATE (i:Policy:DomainObject { name: 'immigration' }) " +
                        "CREATE (j)-[:WRITES_POLICY]->(h) " +
                        "CREATE (j)-[:WRITES_POLICY]->(i) ");


    }


    private void executeStatementsAndAssertSameGraph(Statements cypher, String sameGraphCypher) {

        assertNotNull("The resultant cypher statements shouldn't be null", cypher.getStatements());
        assertFalse("The resultant cypher statements shouldn't be empty", cypher.getStatements().isEmpty());

        for (Statement query : cypher.getStatements()) {
            executionEngine.execute(query.getStatement(), query.getParameters());
        }
        assertSameGraph(getDatabase(), sameGraphCypher);
    }

    private void expect(String expected, Statements cypher) {
        assertEquals(expected, cypher.getStatements().get(0).getStatement());
    }

    private String var(Long nodeId) {
        return "$" + nodeId;
    }

}
