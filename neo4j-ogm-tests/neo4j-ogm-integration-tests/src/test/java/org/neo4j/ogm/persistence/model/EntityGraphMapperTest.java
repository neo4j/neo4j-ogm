/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.persistence.model;

import static org.assertj.core.api.Assertions.*;
import static org.neo4j.ogm.testutil.GraphTestUtils.*;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.ogm.context.EntityGraphMapper;
import org.neo4j.ogm.context.EntityMapper;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.cypher.compiler.Compiler;
import org.neo4j.ogm.domain.blog.Post;
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
import org.neo4j.ogm.domain.types.EntityWithUnmanagedFieldType;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.Statements;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.request.RowStatementFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Adam George
 * @author Luanne Misquitta
 */
public class EntityGraphMapperTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;
    private EntityMapper mapper;

    private static MetaData mappingMetadata;
    private static MappingContext mappingContext;

    private Session session;

    @BeforeClass
    public static void setUpTestDatabase() {

        mappingMetadata = new MetaData(
            "org.neo4j.ogm.domain.education",
            "org.neo4j.ogm.domain.forum",
            "org.neo4j.ogm.domain.social",
            "org.neo4j.ogm.domain.policy");
        mappingContext = new MappingContext(mappingMetadata);
    }

    @Before
    public void setUpMapper() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.policy",
            "org.neo4j.ogm.domain.election", "org.neo4j.ogm.domain.forum",
            "org.neo4j.ogm.domain.education", "org.neo4j.ogm.domain.types");
        mappingContext.clear();
        this.mapper = new EntityGraphMapper(mappingMetadata, mappingContext, false);
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnAttemptToMapNullObjectToCypherQuery() {
        this.mapper.map(null);
    }

    @Test
    public void createObjectWithLabelsAndProperties() {

        Student newStudent = new Student("Gary");

        session.save(newStudent);

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "CREATE (:Student:DomainObject {name:\"Gary\"})");
    }

    @Test
    public void updateObjectPropertyAndLabel() {

        Result executionResult = getGraphDatabaseService()
            .execute("CREATE (s:Student {name:'Sheila Smythe'}) RETURN id(s) AS id");
        Long sid = Long.valueOf(executionResult.next().get("id").toString());

        Student sheila = session.load(Student.class, sid);

        // now update the object's properties locally
        sheila.setName("Sheila Smythe-Jones");

        session.save(sheila);

        GraphTestUtils
            .assertSameGraph(getGraphDatabaseService(), "CREATE (s:DomainObject:Student {name:'Sheila Smythe-Jones'})");
    }

    @Test
    public void doNothingIfNothingHasChanged() {

        // fake-load sheila:
        Result executionResult = getGraphDatabaseService()
            .execute("CREATE (s:Student:DomainObject {name:'Sheila Smythe'}) RETURN id(s) AS id");
        Long existingNodeId = Long.valueOf(executionResult.next().get("id").toString());
        Student sheila = new Student();
        sheila.setId(existingNodeId);
        sheila.setName("Sheila Smythe");
        mappingContext.addNodeEntity(sheila);

        Compiler compiler = this.mapper.map(sheila).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());
        Statements cypher = new Statements(compiler.getAllStatements());

        assertThat(cypher.getStatements()).isEmpty();
    }

    @Test
    public void addObjectToCollection() {

        // fake load one student on a course
        Result executionResult = getGraphDatabaseService().execute(
            "CREATE (c:Course {name:'BSc Computer Science'})-[:STUDENTS]->(s:Student:DomainObject {name:'Gianfranco'}) "
                +
                "RETURN id(s) AS student_id, id(c) AS course_id");

        Map<String, Object> resultSetRow = executionResult.next();
        Long studentId = Long.valueOf(resultSetRow.get("student_id").toString());
        Long courseId = Long.valueOf(resultSetRow.get("course_id").toString());

        Student gianFranco = session.load(Student.class, studentId);
        Course bscComputerScience = session.load(Course.class, courseId);

        // create a new student and set both students on the course
        Student lakshmipathy = new Student("Lakshmipathy");

        bscComputerScience.setStudents(Arrays.asList(lakshmipathy, gianFranco));

        // XXX: NB: currently using a dodgy relationship type because of simple strategy read/write relationship naming inconsistency
        session.save(bscComputerScience);

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "CREATE (c:Course {name:'BSc Computer Science'}), " +
            "(x:Student:DomainObject {name:'Gianfranco'}), (y:Student:DomainObject {name:'Lakshmipathy'}) " +
            "WITH c, x, y MERGE (c)-[:STUDENTS]->(x) MERGE (c)-[:STUDENTS]->(y)");
    }

    @Test
    public void persistManyToOneObjectFromSingletonSide() {

        Result executionResult = getGraphDatabaseService().execute(
            "CREATE (s:School:DomainObject {name:'Waller'})-[:TEACHERS]->(t:Teacher {name:'Mary'})-[:SCHOOL]->(s) " +
                "RETURN id(s) AS school_id, id(t) AS teacher_id");

        Map<String, Object> resultSetRow = executionResult.next();
        Long wallerId = Long.valueOf(resultSetRow.get("school_id").toString());
        Long maryId = Long.valueOf(resultSetRow.get("teacher_id").toString());

        School waller = session.load(School.class, wallerId);

        Teacher mary = session.load(Teacher.class, maryId);
        mary.setId(maryId);

        // create a new teacher and add him to the school
        Teacher jim = new Teacher("Jim");
        jim.setSchool(waller);

        // ensure that the domain objects are mutually established by the code
        assertThat(waller.getTeachers().contains(jim)).isTrue();

        session.save(jim);

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
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

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
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

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "CREATE (t:Teacher {name:'Mrs Kapoor'}), "
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
        Result executionResult = getGraphDatabaseService().execute("CREATE (c:Course {name:'GCSE Music'}), "
            + "(c)-[:STUDENTS]->(x:Student:DomainObject {name:'Xavier'}), "
            + "(c)-[:STUDENTS]->(y:Student:DomainObject {name:'Yvonne'}), "
            + "(c)-[:STUDENTS]->(z:Student:DomainObject {name:'Zack'}) "
            + "RETURN id(c) AS course_id, id(x) AS xid, id(y) AS yid, id(z) AS zid");
        Map<String, ?> results = executionResult.next();
        executionResult.close();

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

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "CREATE (:Student:DomainObject {name:'Xavier'}), "
            + "(:Student:DomainObject {name:'Zack'}), "
            + "(:Course {name:'GCSE Music'})-[:STUDENTS]->(:Student:DomainObject {name:'Yvonne'})");
    }

    @Test
    public void shouldCorrectlyRemoveRelationshipWhenItemIsMovedToDifferentCollection() {
        // start with one teacher teachers two courses, each with one student in
        Result executionResult = getGraphDatabaseService().execute(
            "CREATE (t:Teacher {name:'Ms Thompson'}), " +
                "(bs:Course {name:'GNVQ Business Studies'})-[:STUDENTS]->(s:Student:DomainObject {name:'Shivani'}), " +
                "(dt:Course {name:'GCSE Design & Technology'})-[:STUDENTS]->(j:Student:DomainObject {name:'Jeff'}), " +
                "(t)-[:COURSES]->(bs), (t)-[:COURSES]->(dt) " +
                "RETURN id(t) AS teacher_id, id(bs) AS bs_id, id(dt) AS dt_id, id(s) AS s_id");

        Map<String, ?> results = executionResult.next();

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

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "CREATE (t:Teacher {name:'Ms Thompson'}), " +
            "(bs:Course {name:'GNVQ Business Studies'}), (dt:Course {name:'GCSE Design & Technology'}), " +
            "(dt)-[:STUDENTS]->(j:Student:DomainObject {name:'Jeff'}), " +
            "(dt)-[:STUDENTS]->(s:Student:DomainObject {name:'Shivani'}), " +
            "(t)-[:COURSES]->(bs), (t)-[:COURSES]->(dt)");
    }

    @Test
    @Ignore("Have to investigate why this fails intermittently")
    public void shouldCorrectlyRemoveRelationshipWhenItemIsDisconnectedFromNonOwningSide() {

        Result executionResult = getGraphDatabaseService().execute("CREATE (s:School:DomainObject), "
            + "(s)-[:TEACHERS]->(j:Teacher {name:'Miss Jones'}), "
            + "(s)-[:TEACHERS]->(w:Teacher {name:'Mr White'}) "
            + "RETURN id(s) AS school_id, id(j) AS jones_id, id(w) AS white_id");

        Map<String, ?> results = executionResult.next();
        executionResult.close();

        Long schoolId = (Long) results.get("school_id");
        Long whiteId = (Long) results.get("white_id");
        Long jonesId = (Long) results.get("jones_id");

        School hillsRoad = session.load(School.class, schoolId);

        Teacher mrWhite = session.load(Teacher.class, whiteId);

        Teacher missJones = session.load(Teacher.class, jonesId);

        // Fire Mr White:
        mrWhite.setSchool(null);

        session.save(hillsRoad);

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (w:Teacher {name:'Mr White'}), (j:Teacher {name:'Miss Jones'}), (s:School:DomainObject), (s)-[:TEACHERS]->(j), (j)-[:SCHOOL]->(s)");
    }

    @Test
    public void testVariablePersistenceToDepthZero() {

        Teacher claraOswald = new Teacher();
        Teacher dannyPink = new Teacher();
        School coalHillSchool = new School("Coal Hill");

        coalHillSchool.setTeachers(Arrays.asList(claraOswald, dannyPink));

        session.save(coalHillSchool, 0);

        // we don't expect the teachers to be persisted when persisting the school to depth 0
        GraphTestUtils
            .assertSameGraph(getGraphDatabaseService(), "CREATE (s:School:DomainObject {name:'Coal Hill'}) RETURN s");
    }

    @Test
    public void shouldGenerateCypherToPersistArraysOfPrimitives() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.social");
        session = sessionFactory.openSession();
        Individual individual = new Individual();
        individual.setName("Jeff");
        individual.setAge(41);
        individual.setBankBalance(1000.50f);
        individual.setPrimitiveIntArray(new int[] { 1, 6, 4, 7, 2 });

        session.save(individual);

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (:Individual {name:'Jeff', age:41, bankBalance: 1000.50, code:0, primitiveIntArray:[1,6,4,7,2]})");

        session.clear();

        Individual loadedIndividual = session.load(Individual.class, individual.getId());
        assertThat(loadedIndividual.getPrimitiveIntArray()).isEqualTo(individual.getPrimitiveIntArray());
    }

    @Test
    public void shouldGenerateCypherToPersistByteArray() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.social");
        session = sessionFactory.openSession();
        Individual individual = new Individual();
        individual.setAge(41);
        individual.setBankBalance(1000.50f);
        individual.setPrimitiveByteArray(new byte[] { 1, 2, 3, 4, 5 });

        session.save(individual);
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (:Individual {age:41, bankBalance: 1000.50, code:0, primitiveByteArray:'AQIDBAU='})");

        Result executionResult = getGraphDatabaseService()
            .execute("MATCH (i:Individual) RETURN i.primitiveByteArray AS bytes");
        Map<String, Object> result = executionResult.next();
        executionResult.close();
        assertThat(result.get("bytes")).as("The array wasn't persisted as the correct type")
            .isEqualTo("AQIDBAU="); //Byte arrays are converted to Base64 Strings
    }

    @Test
    public void shouldGenerateCypherToPersistCollectionOfBoxedPrimitivesToArrayOfPrimitives() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.social");
        session = sessionFactory.openSession();
        Individual individual = new Individual();
        individual.setName("Gary");
        individual.setAge(36);
        individual.setBankBalance(99.25f);
        individual.setFavouriteRadioStations(new Vector<>(Arrays.asList(97.4, 105.4, 98.2)));

        session.save(individual);
        assertSameGraph(getGraphDatabaseService(),
            "CREATE (:Individual {name:'Gary', age:36, bankBalance:99.25, code:0, favouriteRadioStations:[97.4, 105.4, 98.2]})");
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
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "CREATE " +
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
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "CREATE " +
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
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "CREATE "
            + "(f:Forum {name:'SDN FAQs'})-[:HAS_TOPIC {timestamp:1647209}]->(t:Topic)");
    }

    @Test
    public void shouldProduceCypherForUpdatingExistingRichRelationshipBetweenNodes() {
        Result executionResult = getGraphDatabaseService().execute(
            "CREATE (f:Forum {name:'Spring Data Neo4j'})-[r:HAS_TOPIC {timestamp:20000}]->(t:Topic {inActive:false}) " +
                "RETURN id(f) AS forumId, id(t) AS topicId, id(r) AS relId");
        Map<String, Object> rs = executionResult.next();
        Long forumId = (Long) rs.get("forumId");
        Long topicId = (Long) rs.get("topicId");
        Long relationshipId = (Long) rs.get("relId");

        Forum forum = session.load(Forum.class, forumId);
        Topic topic = session.load(Topic.class, topicId);
        ForumTopicLink link = session.load(ForumTopicLink.class, relationshipId);
        link.setTimestamp(327790L);
        forum.setTopicsInForum(Arrays.asList(link));

        session.save(forum);
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "CREATE "
            + "(f:Forum {name:'Spring Data Neo4j'})-[r:HAS_TOPIC {timestamp:327790}]->(t:Topic {inActive:false})");
    }

    @org.junit.Ignore
    @Test
    public void shouldSaveCollectionOfRichRelationships() {
        Result executionResult = getGraphDatabaseService().execute("CREATE "
            + "(f:Forum {name:'SDN 4.x'})-[r:HAS_TOPIC]->(t:Topic) RETURN id(f) AS forumId, id(r) AS relId, id(t) AS topicId");
        Map<String, Object> resultSet = executionResult.next();
        executionResult.close();
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
        List<ForumTopicLink> linksToSave = Arrays
            .asList(firstRelationshipEntity, secondRelationshipEntity, thirdRelationshipEntity);

        // FIXME: currently fails straight away, but do we even support mapping collections in this way?
        Statements cypher = new Statements(this.mapper.map(linksToSave).getCompiler().getAllStatements());
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
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (:Person:DomainObject { name :'jim' })-[:WRITES_POLICY]->(:Policy:DomainObject { name: 'health' })");
    }

    @Test
    public void testCreateFirstReferenceFromIncomingSide() {

        Person person1 = new Person("jim");
        Policy policy1 = new Policy("health");

        policy1.getWriters().add(person1);

        session.save(policy1);
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (:Person:DomainObject { name :'jim' })-[:WRITES_POLICY]->(:Policy:DomainObject { name: 'health' })");
    }

    @Test
    public void testDeleteExistingReferenceFromOutgoingSide() {

        Result executionResult = getGraphDatabaseService().execute(
            "CREATE (j:Person:DomainObject { name :'jim' })" +
                "-[r:WRITES_POLICY]->" +
                "(h:Policy:DomainObject { name: 'health' }) " +
                "RETURN id(j) AS jid, id(r) AS rid, id(h) AS hid");

        Map<String, Object> resultSet = executionResult.next();
        executionResult.close();
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
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (:Person:DomainObject { name :'jim' }) " +
                "CREATE (:Policy:DomainObject { name: 'health' })");
    }

    @Test
    public void testDeleteExistingReferenceFromIncomingSide() {

        Result executionResult = getGraphDatabaseService().execute(
            "CREATE (j:Person:DomainObject { name :'jim' })" +
                "-[r:WRITES_POLICY]->" +
                "(h:Policy:DomainObject { name: 'health' }) " +
                "RETURN id(j) AS jid, id(r) AS rid, id(h) AS hid");

        Map<String, Object> resultSet = executionResult.next();
        executionResult.close();
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
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (:Person:DomainObject { name :'jim' }) " +
                "CREATE (:Policy:DomainObject { name: 'health' })");
    }

    @Test
    public void testAppendReferenceFromOutgoingSide() {

        Result executionResult = getGraphDatabaseService().execute(
            "CREATE (j:Person:DomainObject { name :'jim' })" +
                "CREATE (h:Policy:DomainObject { name: 'health' }) " +
                "CREATE (i:Policy:DomainObject { name: 'immigration' }) " +
                "CREATE (j)-[r:WRITES_POLICY]->(h) " +
                "RETURN id(j) AS jid, id(r) AS rid, id(h) AS hid, id(i) AS iid");

        Map<String, Object> resultSet = executionResult.next();
        executionResult.close();
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
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (j:Person:DomainObject { name :'jim' }) " +
                "CREATE (h:Policy:DomainObject { name: 'health' }) " +
                "CREATE (i:Policy:DomainObject { name: 'immigration' }) " +
                "CREATE (j)-[:WRITES_POLICY]->(h) " +
                "CREATE (j)-[:WRITES_POLICY]->(i) ");
    }

    @Test
    public void testAppendReferenceFromIncomingSide() {

        Result executionResult = getGraphDatabaseService().execute(
            "CREATE (j:Person:DomainObject { name :'jim' })" +
                "CREATE (h:Policy:DomainObject { name: 'health' }) " +
                "CREATE (i:Policy:DomainObject { name: 'immigration' }) " +
                "CREATE (j)-[r:WRITES_POLICY]->(h) " +
                "RETURN id(j) AS jid, id(r) AS rid, id(h) AS hid, id(i) AS iid");

        Map<String, Object> resultSet = executionResult.next();
        executionResult.close();

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
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (j:Person:DomainObject { name :'jim' }) " +
                "CREATE (h:Policy:DomainObject { name: 'health' }) " +
                "CREATE (i:Policy:DomainObject { name: 'immigration' }) " +
                "CREATE (j)-[:WRITES_POLICY]->(h) " +
                "CREATE (j)-[:WRITES_POLICY]->(i) ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowMeaningfulErrorMessageWhenLoadingUnscannedEntity() throws Exception {

        session.load(Post.class, 1L);
    }

    // see issue #347
    @Test
    public void shouldNotThrowNpeOnUnknownEntityFieldType() throws Exception {

        EntityWithUnmanagedFieldType entity = new EntityWithUnmanagedFieldType();
        ZonedDateTime now = ZonedDateTime.now();
        entity.setDate(now);
        session.save(entity);
        session.clear();
        EntityWithUnmanagedFieldType loaded = session.load(EntityWithUnmanagedFieldType.class, entity.getId());
        assertThat(loaded).isNotNull();
    }

    private void executeStatementsAndAssertSameGraph(Statements cypher, String sameGraphCypher) {

        assertThat(cypher.getStatements()).as("The resultant cypher statements shouldn't be null").isNotNull();
        assertThat(cypher.getStatements().isEmpty()).as("The resultant cypher statements shouldn't be empty").isFalse();

        for (Statement query : cypher.getStatements()) {
            getGraphDatabaseService().execute(query.getStatement(), query.getParameters());
        }
        assertSameGraph(getGraphDatabaseService(), sameGraphCypher);
    }

    private void expect(String expected, Statements cypher) {
        assertThat(cypher.getStatements().get(0).getStatement()).isEqualTo(expected);
    }

    private String var(Long nodeId) {
        return "$" + nodeId;
    }
}
