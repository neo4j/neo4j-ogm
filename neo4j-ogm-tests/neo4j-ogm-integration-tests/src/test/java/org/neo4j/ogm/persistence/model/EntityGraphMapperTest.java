/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
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
import org.neo4j.ogm.request.Statements;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.request.RowStatementFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Adam George
 * @author Luanne Misquitta
 */
public class EntityGraphMapperTest extends TestContainersTestBase {

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
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.policy",
            "org.neo4j.ogm.domain.election", "org.neo4j.ogm.domain.forum",
            "org.neo4j.ogm.domain.education", "org.neo4j.ogm.domain.types");
        mappingContext.clear();
        this.mapper = new EntityGraphMapper(mappingMetadata, mappingContext);
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

        session.clear();
        assertThat(session.query("MATCH (s:Student:DomainObject {name: 'Gary'}) return s", emptyMap()).queryResults())
            .isNotEmpty();
    }

    @Test
    public void updateObjectPropertyAndLabel() {

        Long sid = (Long) session.query("CREATE (s:Student {name:'Sheila Smythe'}) RETURN id(s) AS id", emptyMap())
            .queryResults().iterator().next().get("id");
        session.clear();

        Student sheila = session.load(Student.class, sid);

        // now update the object's properties locally
        sheila.setName("Sheila Smythe-Jones");

        session.save(sheila);

        session.clear();
        assertThat(session.query("MATCH (s:DomainObject:Student {name:'Sheila Smythe-Jones'}) return s", emptyMap()).queryResults())
            .isNotEmpty();
    }

    @Test
    public void doNothingIfNothingHasChanged() {

        Long existingNodeId =
            (Long) session.query("CREATE (s:Student:DomainObject {name:'Sheila Smythe'}) RETURN id(s) AS id", emptyMap())
            .queryResults().iterator().next().get("id");
        Student sheila = new Student();
        sheila.setId(existingNodeId);
        sheila.setName("Sheila Smythe");
        mappingContext.addNodeEntity(sheila);
        session.clear();

        Compiler compiler = this.mapper.map(sheila).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());
        Statements cypher = new Statements(compiler.getAllStatements());

        assertThat(cypher.getStatements()).isEmpty();
    }

    @Test
    public void addObjectToCollection() {

        // fake load one student on a course
        Iterable<Map<String, Object>> executionResult = session.query(
            "CREATE (c:Course {name:'BSc Computer Science'})-[:STUDENTS]->(s:Student:DomainObject {name:'Gianfranco'}) "
                +
                "RETURN id(s) AS student_id, id(c) AS course_id", emptyMap()).queryResults();

        Map<String, Object> resultSetRow = executionResult.iterator().next();
        Long studentId = Long.valueOf(resultSetRow.get("student_id").toString());
        Long courseId = Long.valueOf(resultSetRow.get("course_id").toString());
        session.clear();

        Student gianFranco = session.load(Student.class, studentId);
        Course bscComputerScience = session.load(Course.class, courseId);

        // create a new student and set both students on the course
        Student lakshmipathy = new Student("Lakshmipathy");

        bscComputerScience.setStudents(Arrays.asList(lakshmipathy, gianFranco));

        // XXX: NB: currently using a dodgy relationship type because of simple strategy read/write relationship naming inconsistency
        session.save(bscComputerScience);

        session.clear();
        assertThat(
            session.queryForObject(Course.class,
                "MATCH (c:Course)-[students:STUDENTS]->(s:Student:DomainObject) return c, students, s",
                emptyMap()).getStudents()).hasSize(2);
    }

    @Test
    public void persistManyToOneObjectFromSingletonSide() {

        Iterable<Map<String, Object>> executionResult = session.query(
            "CREATE (s:School:DomainObject {name:'Waller'})-[:TEACHERS]->(t:Teacher {name:'Mary'})-[:SCHOOL]->(s) " +
                "RETURN id(s) AS school_id, id(t) AS teacher_id", emptyMap()).queryResults();

        Map<String, Object> resultSetRow = executionResult.iterator().next();
        Long wallerId = Long.valueOf(resultSetRow.get("school_id").toString());
        Long maryId = Long.valueOf(resultSetRow.get("teacher_id").toString());
        session.clear();

        School waller = session.load(School.class, wallerId);

        Teacher mary = session.load(Teacher.class, maryId);
        mary.setId(maryId);

        // create a new teacher and add him to the school
        Teacher jim = new Teacher("Jim");
        jim.setSchool(waller);

        // ensure that the domain objects are mutually established by the code
        assertThat(waller.getTeachers().contains(jim)).isTrue();

        session.save(jim);

        session.clear();
        assertThat(session.query("MATCH " +
            "(s:School:DomainObject {name:'Waller'}), " +
            "(m:Teacher {name:'Mary'}), " +
            "(j:Teacher {name:'Jim'}) " +
            "WHERE (j)-[:SCHOOL]->(s) and " +
            "(m)-[:SCHOOL]->(s) and " +
            "(s)-[:TEACHERS]->(j) and " +
            "(s)-[:TEACHERS]->(m) return s, m, j", emptyMap()).queryResults()).hasSize(1);
    }

    @Test
    public void shouldNotGetIntoAnInfiniteLoopWhenSavingObjectsThatReferenceEachOther() {

        Teacher missJones = new Teacher("Miss Jones");
        Teacher mrWhite = new Teacher("Mr White");
        School school = new School("Hilly Fields");
        school.setTeachers(Arrays.asList(missJones, mrWhite));

        session.save(school);

        session.clear();
        assertThat(session.query(
            "MATCH (j:Teacher {name:'Miss Jones'}), " +
            "(w:Teacher {name:'Mr White'}), " +
            "(s:School:DomainObject {name:'Hilly Fields'}) " +
            "WHERE (s)-[:TEACHERS]->(j)-[:SCHOOL]->(s) and " +
            "(s)-[:TEACHERS]->(w)-[:SCHOOL]->(s) return j, w, s", emptyMap()).queryResults()).hasSize(1);
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

        session.clear();
        assertThat(session.query("MATCH (t:Teacher {name:'Mrs Kapoor'}), "
            + "(p:Course {name:'GCSE Physics'}), (m:Course {name:'A-Level Mathematics'}), "
            + "(s:Student:DomainObject {name:'Sheila Smythe'}), "
            + "(g:Student:DomainObject {name:'Gary Jones'}), "
            + "(w:Student:DomainObject {name:'Winston Charles'}) "
            + "WHERE (t)-[:COURSES]->(p)-[:STUDENTS]->(s) and (t)-[:COURSES]->(m)-[:STUDENTS]->(s) and "
            + "(p)-[:STUDENTS]->(g) and (m)-[:STUDENTS]->(w) return t, p, m, s, g, w", emptyMap()).queryResults()).hasSize(1);
    }

    @Test
    public void shouldCorrectlyRemoveRelationshipWhenItemIsRemovedFromCollection() {
        // simple music course with three students
        Iterable<Map<String,Object>> executionResult = session.query("CREATE (c:Course {name:'GCSE Music'}), "
            + "(c)-[:STUDENTS]->(x:Student:DomainObject {name:'Xavier'}), "
            + "(c)-[:STUDENTS]->(y:Student:DomainObject {name:'Yvonne'}), "
            + "(c)-[:STUDENTS]->(z:Student:DomainObject {name:'Zack'}) "
            + "RETURN id(c) AS course_id, id(x) AS xid, id(y) AS yid, id(z) AS zid", emptyMap()).queryResults();
        Map<String, ?> results = executionResult.iterator().next();
        session.clear();

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

        session.clear();
        assertThat(session.query("MATCH (a:Student:DomainObject {name:'Xavier'}), "
            + "(b:Student:DomainObject {name:'Zack'}), "
            + "(c:Course {name:'GCSE Music'})-[:STUDENTS]->(:Student:DomainObject {name:'Yvonne'}) return a,b,c",
            emptyMap()).queryResults()).hasSize(1);
    }

    @Test
    public void shouldCorrectlyRemoveRelationshipWhenItemIsMovedToDifferentCollection() {
        // start with one teacher teachers two courses, each with one student in
        Iterable<Map<String, Object>> executionResult = session.query(
            "CREATE (t:Teacher {name:'Ms Thompson'}), " +
                "(bs:Course {name:'GNVQ Business Studies'})-[:STUDENTS]->(s:Student:DomainObject {name:'Shivani'}), " +
                "(dt:Course {name:'GCSE Design & Technology'})-[:STUDENTS]->(j:Student:DomainObject {name:'Jeff'}), " +
                "(t)-[:COURSES]->(bs), (t)-[:COURSES]->(dt) " +
                "RETURN id(t) AS teacher_id, id(bs) AS bs_id, id(dt) AS dt_id, id(s) AS s_id", emptyMap()).queryResults();

        Map<String, ?> results = executionResult.iterator().next();

        Long teacherId = (Long) results.get("teacher_id");
        Long businessStudiesCourseId = (Long) results.get("bs_id");
        Long designTechnologyCourseId = (Long) results.get("dt_id");
        Long studentId = (Long) results.get("s_id");
        session.clear();

        Course designTech = session.load(Course.class, designTechnologyCourseId);

        Course businessStudies = session.load(Course.class, businessStudiesCourseId);

        Teacher msThompson = session.load(Teacher.class, teacherId);

        Student shivani = session.load(Student.class, studentId);

        // move student from one course to the other
        businessStudies.setStudents(Collections.<Student>emptyList());
        designTech.getStudents().add(shivani);

        session.save(msThompson);

        session.clear();
        assertThat(session.query("MATCH (t:Teacher {name:'Ms Thompson'}), " +
            "(bs:Course {name:'GNVQ Business Studies'}), (dt:Course {name:'GCSE Design & Technology'}) " +
            "WHERE (dt)-[:STUDENTS]->(:Student:DomainObject {name:'Jeff'}) and " +
            "(dt)-[:STUDENTS]->(:Student:DomainObject {name:'Shivani'}) and " +
            "(t)-[:COURSES]->(bs) and (t)-[:COURSES]->(dt) return t, bs, dt", emptyMap()).queryResults()).hasSize(1);
    }

    @Test
    public void testVariablePersistenceToDepthZero() {

        Teacher claraOswald = new Teacher();
        Teacher dannyPink = new Teacher();
        School coalHillSchool = new School("Coal Hill");

        coalHillSchool.setTeachers(Arrays.asList(claraOswald, dannyPink));

        session.save(coalHillSchool, 0);

        // we don't expect the teachers to be persisted when persisting the school to depth 0
        session.clear();
        assertThat(session.query("MATCH (s:School:DomainObject {name:'Coal Hill'}) RETURN s", emptyMap())
            .queryResults()).hasSize(1);
        assertThat(session.query("MATCH (t:Teacher) RETURN t", emptyMap())
            .queryResults()).hasSize(0);
    }

    @Test
    public void shouldGenerateCypherToPersistArraysOfPrimitives() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.social");
        session = sessionFactory.openSession();
        Individual individual = new Individual();
        individual.setName("Jeff");
        individual.setAge(41);
        individual.setBankBalance(1000.50f);
        individual.setPrimitiveIntArray(new int[] { 1, 6, 4, 7, 2 });

        session.save(individual);

        session.clear();
        Individual loadedIndividual = session.load(Individual.class, individual.getId());
        assertThat(loadedIndividual.getPrimitiveIntArray()).isEqualTo(individual.getPrimitiveIntArray());
    }

    @Test
    public void shouldGenerateCypherToPersistByteArray() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.social");
        session = sessionFactory.openSession();
        Individual individual = new Individual();
        individual.setAge(41);
        individual.setBankBalance(1000.50f);
        individual.setPrimitiveByteArray(new byte[] { 1, 2, 3, 4, 5 });

        session.save(individual);
        session.query("CREATE (:Individual {age:41, bankBalance: 1000.50, code:0, primitiveByteArray:'AQIDBAU='})",
            emptyMap());

        session.clear();
        Iterable<Map<String, Object>> executionResult = session.query("MATCH (i:Individual) RETURN i.primitiveByteArray AS bytes", emptyMap())
            .queryResults();
        Map<String, Object> result = executionResult.iterator().next();

        assertThat(result.get("bytes")).as("The array wasn't persisted as the correct type")
            .isEqualTo("AQIDBAU="); //Byte arrays are converted to Base64 Strings
    }

    @Test
    public void shouldGenerateCypherToPersistCollectionOfBoxedPrimitivesToArrayOfPrimitives() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.social");
        session = sessionFactory.openSession();
        Individual individual = new Individual();
        individual.setName("Gary");
        individual.setAge(36);
        individual.setBankBalance(99.25f);
        individual.setFavouriteRadioStations(new Vector<>(Arrays.asList(97.4, 105.4, 98.2)));

        session.save(individual);
        session.clear();
        assertThat(
            session.query(
                "MATCH (a:Individual "
                    + "{name:'Gary', age:36, bankBalance:99.25, code:0, favouriteRadioStations:[97.4, 105.4, 98.2]}) "
                    + "return a",
                emptyMap()).queryResults()).hasSize(1);
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
        session.clear();
        assertThat(session.query("MATCH " +
            "(s:School:DomainObject {name:'Coal Hill'}), " +
            "(c:Teacher {name:'Clara Oswald'}), " +
            "(d:Teacher {name:'Danny Pink'}) " +
            "WHERE (s)-[:TEACHERS]->(c) and " +
            "(s)-[:TEACHERS]->(d) return s,c,d", emptyMap())).hasSize(1);

        assertThat(session.query("MATCH (c:Course) return c", emptyMap())).hasSize(0);
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
        session.clear();
        assertThat(session.query("MATCH" +
            "(school:School:DomainObject {name:'Coal Hill'}), " +
            "(clara:Teacher {name:'Clara Oswald'}), " +
            "(danny:Teacher {name:'Danny Pink'}), " +
            "(english:Course {name:'English'}), " +
            "(maths:Course {name:'Maths'}) " +
            "WHERE (school)-[:TEACHERS]->(clara)-[:SCHOOL]->(school) and " +
            "(school)-[:TEACHERS]->(danny)-[:SCHOOL]->(school) and " +
            "(danny)-[:COURSES]->(maths) and " +
            "(clara)-[:COURSES]->(english) return school, clara, danny, english, maths", emptyMap())
            .queryResults()).hasSize(1);
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

        session.clear();
        assertThat(
            session.query("MATCH (f:Forum {name:'SDN FAQs'})-[:HAS_TOPIC {timestamp:1647209}]->(t:Topic) return f",
                emptyMap()).queryResults())
            .hasSize(1);
    }

    @Test
    public void shouldProduceCypherForUpdatingExistingRichRelationshipBetweenNodes() {
        Iterable<Map<String, Object>> executionResult = session.query(
            "CREATE (f:Forum {name:'Spring Data Neo4j'})-[r:HAS_TOPIC {timestamp:20000}]->(t:Topic {inActive:false}) " +
                "RETURN id(f) AS forumId, id(t) AS topicId, id(r) AS relId", emptyMap()).queryResults();
        Map<String, Object> rs = executionResult.iterator().next();
        Long forumId = (Long) rs.get("forumId");
        Long topicId = (Long) rs.get("topicId");
        Long relationshipId = (Long) rs.get("relId");
        session.clear();

        Forum forum = session.load(Forum.class, forumId);
        Topic topic = session.load(Topic.class, topicId);
        ForumTopicLink link = session.load(ForumTopicLink.class, relationshipId);
        link.setTimestamp(327790L);
        forum.setTopicsInForum(Arrays.asList(link));

        session.save(forum);
        session.clear();
        assertThat(session.query(
            "MATCH (f:Forum {name:'Spring Data Neo4j'})-[r:HAS_TOPIC {timestamp:327790}]->(t:Topic {inActive:false}) "
                + "return f",
            emptyMap()).queryResults()).hasSize(1);
    }

    @Test
    public void testCreateFirstReferenceFromOutgoingSide() {

        Person person1 = new Person("jim");
        Policy policy1 = new Policy("health");

        person1.getWritten().add(policy1);

        session.save(person1);

        session.clear();
        assertThat(session.query(
            "MATCH (p:Person:DomainObject { name :'jim' })-[:WRITES_POLICY]->(:Policy:DomainObject { name: 'health' }) "
                + "return p", emptyMap()).queryResults())
            .hasSize(1);
    }

    @Test
    public void testCreateFirstReferenceFromIncomingSide() {

        Person person1 = new Person("jim");
        Policy policy1 = new Policy("health");

        policy1.getWriters().add(person1);

        session.save(policy1);
        session.clear();
        assertThat(session.query(
            "MERGE (p:Person:DomainObject { name :'jim' })-[:WRITES_POLICY]->(:Policy:DomainObject { name: 'health' }) "
                + "return p", emptyMap()).queryResults())
            .hasSize(1);
    }

    @Test
    public void testDeleteExistingReferenceFromOutgoingSide() {

        Iterable<Map<String, Object>> executionResult = session.query(
            "CREATE (j:Person:DomainObject { name :'jim' })" +
                "-[r:WRITES_POLICY]->" +
                "(h:Policy:DomainObject { name: 'health' }) " +
                "RETURN id(j) AS jid, id(r) AS rid, id(h) AS hid", emptyMap()).queryResults();

        Map<String, Object> resultSet = executionResult.iterator().next();
        Long jid = (Long) resultSet.get("jid");
        Long hid = (Long) resultSet.get("hid");
        session.clear();

        Person person = session.load(Person.class, jid);

        Policy policy = session.load(Policy.class, hid);
        policy.setId(hid);

        // ensure domain model is set up
        policy.getWriters().add(person);
        person.getWritten().add(policy);

        // now remove the relationship from the person side
        person.getWritten().clear();

        session.save(person);
        session.clear();
        assertThat(session.query("MATCH (pe:Person:DomainObject { name :'jim' }), "
            + "(po:Policy:DomainObject { name: 'health' }) return pe, po", emptyMap()).queryResults())
            .hasSize(1);
    }

    @Test
    public void testDeleteExistingReferenceFromIncomingSide() {

        Iterable<Map<String, Object>> executionResult = session.query(
            "CREATE (j:Person:DomainObject { name :'jim' })" +
                "-[r:WRITES_POLICY]->" +
                "(h:Policy:DomainObject { name: 'health' }) " +
                "RETURN id(j) AS jid, id(r) AS rid, id(h) AS hid", emptyMap()).queryResults();

        Map<String, Object> resultSet = executionResult.iterator().next();
        Long jid = (Long) resultSet.get("jid");
        Long hid = (Long) resultSet.get("hid");
        session.clear();

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
        session.clear();
        assertThat(session.query("MATCH (pe:Person:DomainObject { name :'jim' }), " +
                "(po:Policy:DomainObject { name: 'health' }) return pe, po", emptyMap()).queryResults())
                .hasSize(1);
    }

    @Test
    public void testAppendReferenceFromOutgoingSide() {

        Iterable<Map<String, Object>> executionResult = session.query(
            "CREATE (j:Person:DomainObject { name :'jim' })" +
                "CREATE (h:Policy:DomainObject { name: 'health' }) " +
                "CREATE (i:Policy:DomainObject { name: 'immigration' }) " +
                "CREATE (j)-[r:WRITES_POLICY]->(h) " +
                "RETURN id(j) AS jid, id(r) AS rid, id(h) AS hid, id(i) AS iid", emptyMap()).queryResults();

        Map<String, Object> resultSet = executionResult.iterator().next();
        Long jid = (Long) resultSet.get("jid");
        Long hid = (Long) resultSet.get("hid");
        Long iid = (Long) resultSet.get("iid");
        session.clear();

        Person jim = session.load(Person.class, jid);

        Policy health = session.load(Policy.class, hid);

        Policy immigration = session.load(Policy.class, iid);

        // set jim as the writer of the health policy and expect the new relationship to be established
        // alongside the existing one.
        jim.getWritten().add(health);
        jim.getWritten().add(immigration);

        session.save(jim);
        session.clear();
        assertThat(session.query("MATCH (j:Person:DomainObject { name :'jim' }), " +
                "(h:Policy:DomainObject { name: 'health' }), " +
                "(i:Policy:DomainObject { name: 'immigration' }) " +
                "WHERE (j)-[:WRITES_POLICY]->(h) and " +
                "(j)-[:WRITES_POLICY]->(i) return j, h, i", emptyMap()).queryResults())
                .hasSize(1);
    }

    @Test
    public void testAppendReferenceFromIncomingSide() {

        Iterable<Map<String, Object>> executionResult = session.query(
            "CREATE (j:Person:DomainObject { name :'jim' })" +
                "CREATE (h:Policy:DomainObject { name: 'health' }) " +
                "CREATE (i:Policy:DomainObject { name: 'immigration' }) " +
                "CREATE (j)-[r:WRITES_POLICY]->(h) " +
                "RETURN id(j) AS jid, id(r) AS rid, id(h) AS hid, id(i) AS iid", emptyMap()).queryResults();

        Map<String, Object> resultSet = executionResult.iterator().next();

        Long jid = (Long) resultSet.get("jid");
        Long hid = (Long) resultSet.get("hid");
        Long iid = (Long) resultSet.get("iid");
        session.clear();

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
        session.clear();
        assertThat(session.query("MATCH (j:Person:DomainObject { name :'jim' }), " +
                "(h:Policy:DomainObject { name: 'health' }), " +
                "(i:Policy:DomainObject { name: 'immigration' }) " +
                "WHERE (j)-[:WRITES_POLICY]->(h) and" +
                "(j)-[:WRITES_POLICY]->(i) return j, h, i", emptyMap()).queryResults())
                .hasSize(1);
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

}
