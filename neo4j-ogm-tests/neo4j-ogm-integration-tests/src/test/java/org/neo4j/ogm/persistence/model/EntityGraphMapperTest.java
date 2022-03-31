/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.assertj.core.data.Index;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
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
import org.neo4j.ogm.domain.forum.Member;
import org.neo4j.ogm.domain.forum.Topic;
import org.neo4j.ogm.domain.policy.Person;
import org.neo4j.ogm.domain.policy.Policy;
import org.neo4j.ogm.domain.social.Individual;
import org.neo4j.ogm.domain.types.EntityWithUnmanagedFieldType;
import org.neo4j.ogm.exception.core.InvalidRelationshipTargetException;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.persistence.examples.versioned_rel_entity.A;
import org.neo4j.ogm.persistence.examples.versioned_rel_entity.B;
import org.neo4j.ogm.persistence.examples.versioned_rel_entity.R;
import org.neo4j.ogm.request.Statements;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.request.RowStatementFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Adam George
 * @author Luanne Misquitta
 * @author Michael J. Simons
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
            "org.neo4j.ogm.domain.education", "org.neo4j.ogm.domain.types", "org.neo4j.ogm.persistence.examples.versioned_rel_entity");
        mappingContext.clear();
        this.mapper = new EntityGraphMapper(mappingMetadata, mappingContext);
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnAttemptToMapNullObjectToCypherQuery() {
        this.mapper.map(null);
    }

    @Test // GH-903
    public void shouldOnlyTouchChangedRelEntities() {

        A a = new A("a1");
        B b = new B("b1");
        R r = a.add(b);
        r.setSomeAttribute("R1");

        Session initial = sessionFactory.openSession();
        initial.save(a);

        Long version = getRelVersion(initial, a, r, b);
        assertThat(version).isOne();

        B b2 = new B("b2");
        R r2 = a.add(b2);
        r2.setSomeAttribute("R2");
        initial.save(a);

        version = getRelVersion(initial, a, r2, b2);
        assertThat(version).isOne();

        version = getRelVersion(initial, a, r, b);
        assertThat(version).isOne();

        r.setSomeAttribute("updateAttribute");
        initial.save(a);

        version = getRelVersion(initial, a, r, b);
        assertThat(version).isEqualTo(2L);
    }

    static Long getRelVersion(Session session, A a, R r, B b) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id1", a.getId());
        parameters.put("id2", b.getId());
        parameters.put("id3", r.getId());

        return session.queryForObject(Long.class, "MATCH (a:A {id: $id1}) <-[r]- (b:B) WHERE id(b) = $id2 AND id(r) = $id3 RETURN r.version",parameters);
    }

    @Test // GH-786
    public void shouldNotWriteReadOnlyProperties() {
        Member test123 = new Member();
        test123.setUserName("Test123");
        test123.setSomeComputedValue("x");
        session.save(test123);

        Iterable<Member> members = sessionFactory.openSession()
            .query(Member.class, "MATCH (n:User {userName: 'Test123'}) RETURN n", Collections.emptyMap());
        assertThat(members).hasSize(1)
            .first().extracting(Member::getSomeComputedValue).isNull();
    }

    @Test // GH-786
    public void shouldReadReadOnlyProperties() {

        session.query("CREATE (n:User {userName: 'Test123', someComputedValue: 'x'})", Collections.emptyMap());

        Iterable<Member> members = sessionFactory.openSession()
            .query(Member.class, "MATCH (n:User {userName: 'Test123'}) RETURN n", Collections.emptyMap());
        assertThat(members).hasSize(1)
            .first().extracting(Member::getSomeComputedValue).isEqualTo("x");
    }

    @Test // GH-786
    public void shouldReadVirtualProperties() {

        boolean apocInstalled;
        try {
            apocInstalled =
                session.queryForObject(Long.class, "call apoc.help('apoc.create.vNode') yield name return count(name)",
                    Collections.emptyMap()) > 0;
        } catch (Exception e) {
            apocInstalled = false;
        }

        Assume.assumeTrue(apocInstalled);

        session.query("CREATE (n:User {userName: 'Test123'})", Collections.emptyMap());

        Iterable<Member> members = session.query(Member.class, "MATCH (n:User {userName: 'Test123'}) \n"
            + "WITH 'a value' as someComputedValue, n\n"
            + "CALL apoc.create.vNode(labels(n), n{.*, someComputedValue}) YIELD node\n"
            + "RETURN node\n", Collections.emptyMap());
        assertThat(members).hasSize(1)
            .first().extracting(Member::getSomeComputedValue).isEqualTo("a value");
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

        // This does only work in non-strict querying
        Session customSession = new SessionFactory(getDriver(), false, "org.neo4j.ogm.domain.education").openSession();

        Long sid = (Long) customSession.query("CREATE (s:Student {name:'Sheila Smythe'}) RETURN id(s) AS id", emptyMap())
            .queryResults().iterator().next().get("id");
        customSession.clear();

        Student sheila = customSession.load(Student.class, sid);

        // now update the object's properties locally
        sheila.setName("Sheila Smythe-Jones");

        customSession.save(sheila);

        customSession.clear();
        assertThat(customSession.query("MATCH (s:DomainObject:Student {name:'Sheila Smythe-Jones'}) return s", emptyMap()).queryResults())
            .isNotEmpty();
    }

    @Test
    public void doNothingIfNothingHasChanged() {

        Long existingNodeId =
            (Long) session.query("CREATE (s:Student:DomainObject {name:'Sheila Smythe'}) RETURN id(s) AS id",
                    emptyMap())
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
        assertThat(session.query(
            "MATCH (s:School:DomainObject {name:'Waller'}) " +
            "MATCH (m:Teacher {name:'Mary'})-[:SCHOOL]->(s) " +
            "MATCH (s)-[:TEACHERS]->(j:Teacher {name:'Jim'}) " +
            "WHERE exists((j)-[:SCHOOL]->(s)) and " +
            "exists((s)-[:TEACHERS]->(m)) return s, m, j", emptyMap()).queryResults()).hasSize(1);
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
            "MATCH (s:School:DomainObject {name:'Hilly Fields'}) " +
            "MATCH (s)-[:TEACHERS]->(j:Teacher {name:'Miss Jones'})-[:SCHOOL]->(s) " +
            "MATCH (s)-[:TEACHERS]->(w:Teacher {name:'Mr White'})-[:SCHOOL]->(s)" +
            "return j, w, s", emptyMap()).queryResults()).hasSize(1);
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
        assertThat(session.query(
                  "MATCH (t:Teacher {name:'Mrs Kapoor'}) "
                + "MATCH (t)-[:COURSES]->(p:Course {name:'GCSE Physics'}) "
                + "MATCH (t)-[:COURSES]->(m:Course {name:'A-Level Mathematics'}) "
                + "MATCH (p)-[:STUDENTS]->(s:Student:DomainObject {name:'Sheila Smythe'})<-[:STUDENTS]-(m) "
                + "MATCH (p)-[:STUDENTS]->(g:Student:DomainObject {name:'Gary Jones'}) "
                + "MATCH (m)-[:STUDENTS]->(w:Student:DomainObject {name:'Winston Charles'}) "
                + "return t, p, m, s, g, w", emptyMap())
            .queryResults()).hasSize(1);
    }

    @Test
    public void shouldCorrectlyRemoveRelationshipWhenItemIsRemovedFromCollection() {
        // simple music course with three students
        Iterable<Map<String, Object>> executionResult = session.query("CREATE (c:Course {name:'GCSE Music'}), "
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

        music.setStudents(List.of(yvonne, xavier, zack));

        // object is now "loaded"
        // now, update the domain model, setting yvonne as the only music student (i.e remove zack and xavier)
        music.setStudents(List.of(yvonne));

        session.save(music);

        session.clear();
        assertThat(session.query("MATCH (a:Student:DomainObject {name:'Xavier'}) "
                + "MATCH (b:Student:DomainObject {name:'Zack'}) "
                + "MATCH (c:Course {name:'GCSE Music'})-[:STUDENTS]->(:Student:DomainObject {name:'Yvonne'}) return a,b,c",
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
                    "RETURN id(t) AS teacher_id, id(bs) AS bs_id, id(dt) AS dt_id, id(s) AS s_id", emptyMap())
            .queryResults();

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
        assertThat(session.query("MATCH (t:Teacher {name:'Ms Thompson'}) -[:COURSES]->(bs:Course {name:'GNVQ Business Studies'}) "
            + "MATCH (t)-[:COURSES]->(dt:Course {name:'GCSE Design & Technology'}) "
            + "WHERE exists((dt)-[:STUDENTS]->(:Student:DomainObject {name:'Jeff'})) and "
            + "exists((dt)-[:STUDENTS]->(:Student:DomainObject {name:'Shivani'})) "
            + "return t, bs, dt", emptyMap()).queryResults()).hasSize(1);
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
        Iterable<Map<String, Object>> executionResult = session.query(
                "MATCH (i:Individual) RETURN i.primitiveByteArray AS bytes", emptyMap())
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
        assertThat(session.query("MATCH (s:School:DomainObject {name:'Coal Hill'})-[:TEACHERS]->(c:Teacher {name:'Clara Oswald'}) "
            + "MATCH (s)-[:TEACHERS]->(d:Teacher {name:'Danny Pink'}) "
            + "return s,c,d", emptyMap())).hasSize(1);

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
        assertThat(session.query("MATCH (school:School:DomainObject {name:'Coal Hill'})-[:TEACHERS]->(clara:Teacher {name:'Clara Oswald'}) "
                + "MATCH (school)-[:TEACHERS]->(danny:Teacher {name:'Danny Pink'}) "
                + "MATCH (clara)-[:COURSES]->(english:Course {name:'English'}) "
                + "MATCH (danny)-[:COURSES]->(maths:Course {name:'Maths'}) "
                + "WHERE exists((clara)-[:SCHOOL]->(school)) and "
                + "exists((danny)-[:SCHOOL]->(school)) "
                + "return school, clara, danny, english, maths", emptyMap())
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
        assertThat(session.query(
            "MATCH (pe:Person:DomainObject { name :'jim' })" +
            "WITH pe MATCH (po:Policy:DomainObject { name: 'health' }) return pe, po", emptyMap()).queryResults())
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
        assertThat(session.query(
            "MATCH (pe:Person:DomainObject { name :'jim' }) " +
            "WITH pe MATCH (po:Policy:DomainObject { name: 'health' }) return pe, po", emptyMap()).queryResults())
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
        assertThat(session.query(
            "MATCH (j:Person:DomainObject { name :'jim' })-[:WRITES_POLICY]-> (h:Policy:DomainObject { name: 'health' }), " +
            "(j)-[:WRITES_POLICY]->(i:Policy:DomainObject { name: 'immigration' }) " +
            "return j, h, i",
            emptyMap()).queryResults()
        ).hasSize(1);
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
        assertThat(session.query("MATCH (j:Person:DomainObject { name :'jim' })-[:WRITES_POLICY]->(h:Policy:DomainObject { name: 'health' }) "
            + "MATCH (j)-[:WRITES_POLICY]->(i:Policy:DomainObject { name: 'immigration' }) "
            + "return j, h, i", emptyMap()).queryResults())
            .hasSize(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowMeaningfulErrorMessageWhenLoadingUnscannedEntity() {

        session.load(Post.class, 1L);
    }

    @Test // GH-781
    public void shouldThrowInvalidRelationshipTargetExceptionOnNullElements() {

        Course course = new Course("Some course");
        Student student1 = new Student("A student");
        Student student2 = new Student("Another student");
        course.setStudents(Arrays.asList(student1, null, student2));

        assertThatExceptionOfType(InvalidRelationshipTargetException.class)
            .isThrownBy(() -> session.save(course))
            .withMessage(
                "The relationship 'STUDENTS' from 'org.neo4j.ogm.domain.education.Course' to 'org.neo4j.ogm.domain.education.Student' stored on '#students' contains 'null', which is an invalid target for this relationship.'");

    }

    @Test // GH-347
    public void shouldNotThrowNpeOnUnknownEntityFieldType() {

        EntityWithUnmanagedFieldType entity = new EntityWithUnmanagedFieldType();
        ZonedDateTime now = ZonedDateTime.now();
        entity.setDate(now);
        session.save(entity);
        session.clear();
        EntityWithUnmanagedFieldType loaded = session.load(EntityWithUnmanagedFieldType.class, entity.getId());
        assertThat(loaded).isNotNull();
    }

    @Test // GH-821
    public void shouldNotMessUpNestedLists() {

        Result result = session.query("RETURN [[0,1,2], [3], [4], [5,6]] as nested_list", Collections.emptyMap());
        assertThat(result).hasSize(1).first().satisfies(row -> {
            Object nestedLists = row.get("nested_list");
            assertThat(nestedLists).isInstanceOf(Long[][].class);

            Long[][] columns = (Long[][]) nestedLists;
            assertThat(columns).hasSize(4);
            assertThat(columns[0]).isInstanceOf(Long[].class)
                .satisfies(c -> assertThat(((Long[]) c)).containsExactly(0L, 1L, 2L));
            assertThat(columns[1]).isInstanceOf(Long[].class)
                .satisfies(c -> assertThat(((Long[]) c)).containsExactly(3L));
        });
    }

    @Test // GH-821
    public void shouldNotMessUpMixedNestedLists() {

        Result result = session.query("RETURN [[0,1,2], [[23,42]], [4], [5,6]] AS nested_list", Collections.emptyMap());
        assertThat(result).hasSize(1).first().satisfies(row -> {
            Object nestedLists = row.get("nested_list");
            assertThat(nestedLists).isInstanceOf(Object[].class);

            Object[] columns = (Object[]) nestedLists;
            assertThat(columns).hasSize(4);
            assertThat(columns[0]).isInstanceOf(Long[].class)
                .satisfies(c -> assertThat(((Long[]) c)).containsExactly(0L, 1L, 2L));
            assertThat(columns[1]).isInstanceOf(Long[][].class)
                .satisfies(c -> assertThat(((Long[][]) c)[0]).containsExactly(23L, 42L));
        });
    }

    @Test // GH-821
    public void shouldNotMessUpMixedNestedCollectedLists() {

        Result result = session.query("UNWIND range(0,2) AS x WITH collect(x) AS x RETURN collect(x) AS nested_list",
            Collections.emptyMap());
        assertThat(result).hasSize(1).first().satisfies(row -> {
            Object nestedLists = row.get("nested_list");
            assertThat(nestedLists).isInstanceOf(Object[].class);

            Object[] columns = (Object[]) nestedLists;
            assertThat(columns).hasSize(1);
            assertThat(columns[0]).isInstanceOf(Long[].class)
                .satisfies(c -> assertThat(((Long[]) c)).containsExactly(0L, 1L, 2L));
        });
    }

    @Test // GH-821
    public void shouldNotMessUpMixedTypedLists() {

        Teacher jim = new Teacher("Jim");
        sessionFactory.openSession().save(jim);

        Result result = session
            .query("MATCH (n:Teacher) RETURN n, [[0,1,2], [[\"a\",\"b\"]], [\"c\"], \"d\"] as nested_list",
                Collections.emptyMap());
        assertThat(result).hasSize(1).first().satisfies(row -> {
            Object nestedLists = row.get("nested_list");
            assertThat(nestedLists).isInstanceOf(Object[].class);

            Object[] columns = (Object[]) nestedLists;
            assertThat(columns).hasSize(4);
            assertThat(columns[0]).isInstanceOf(Long[].class)
                .satisfies(c -> assertThat(((Long[]) c)).containsExactly(0L, 1L, 2L));
            assertThat(columns[1]).isInstanceOf(String[][].class)
                .satisfies(c -> assertThat(((String[][]) c)[0]).containsExactly("a", "b"));

            assertThat(columns[2]).isInstanceOf(String[].class)
                .satisfies(c -> assertThat(((String[]) c)).containsExactly("c"));

            assertThat(columns[3]).isInstanceOf(String.class)
                .isEqualTo("d");

            assertThat(row.get("n"))
                .isInstanceOf(Teacher.class)
                .extracting("name").first().isEqualTo("Jim");
        });
    }

    @Test // GH-902
    @SuppressWarnings("unchecked")
    public void nestedListsWithDomainModel() {

        Session writingSession = sessionFactory.openSession();
        for (int i = 0; i < 2; ++i) {
            Teacher jim = new Teacher("T" + i);
            writingSession.save(jim);
        }

        Result result = session
            .query("match (m:Teacher) with collect(m) as x return collect(x) as listOfListsOfThings",
                Collections.emptyMap());

        assertThat(result).hasSize(1).first().satisfies(row -> {
            Object nestedLists = row.get("listOfListsOfThings");
            assertThat(nestedLists).isInstanceOf(List.class);
            assertThat((List<?>) nestedLists)
                .hasSize(1).first()
                .satisfies(EntityGraphMapperTest::assertContentOfList);
        });
    }

    @Test // GH-902
    @SuppressWarnings("unchecked")
    public void nestedNestedLists() {

        Session writingSession = sessionFactory.openSession();
        for (int i = 0; i < 3; ++i) {
            Teacher jim = new Teacher("T" + i);
            writingSession.save(jim);
        }

        Result result = session
            .query("match (m:Teacher) where m.name = 'T0' or m.name = 'T1' with collect(m) as x \n"
                    + "match (m:Teacher {name: \"T2\"}) with x, collect(m) as y\n"
                    + "with [x,y] as x\n"
                    + "return collect(x) as listOfListsOfThings",
                Collections.emptyMap());

        assertThat(result).hasSize(1).first().satisfies(row -> {
            Object nestedLists = row.get("listOfListsOfThings");
            assertThat(nestedLists).isInstanceOf(List.class);
            assertThat((List<?>) nestedLists)
                .hasSize(1)
                .first()
                .satisfies(firstInnerList -> {
                    assertThat(firstInnerList).isInstanceOf(List.class);
                    assertThat((List<List<?>>) firstInnerList).hasSize(2)
                        .satisfies(EntityGraphMapperTest::assertContentOfList, Index.atIndex(0))
                        .satisfies(l -> assertThat(l).hasSize(1)
                            .extracting(v -> ((Teacher) v).getName()).containsExactly("T2"), Index.atIndex(1));
                });
        });
    }

    @SuppressWarnings("unchecked")
    private static void assertContentOfList(Object firstInnerList) {
        assertThat(firstInnerList).isInstanceOf(List.class);
        assertThat((List<Teacher>) firstInnerList)
            .extracting(Teacher::getName).containsExactlyInAnyOrder("T0", "T1");
    }

    @Test // GH-902
    @SuppressWarnings("unchecked")
    public void collectedListOfNodesFromPathsShouldNotCollapse() {
        Teacher t = new Teacher("T0");
        School s = new School("Sß");
        Course c = new Course("C0");

        t.setSchool(s);
        t.setCourses(singletonList(c));

        Session writingSession = sessionFactory.openSession();
        writingSession.save(t);

        Result result = session.query("MATCH p=(:School) -[*]->(:Course) RETURN COLLECT (DISTINCT nodes(p)) AS paths",
            Collections.emptyMap());
        assertThat(result).hasSize(1).first().satisfies(row -> {
            Object nestedLists = row.get("paths");
            assertThat(nestedLists).isInstanceOf(List.class);
            assertThat((List<?>) nestedLists).hasSize(1)
                .first()
                .satisfies(v -> {
                    assertThat(v).isInstanceOf(List.class);
                    assertThat((List<?>) v)
                        .extracting(Object::getClass)
                        .extracting(Class::getSimpleName)
                        .containsExactly("School", "Teacher", "Course");
                });
        });
    }

    @Test
    public void emptyArray1Level() {

        Result result = session.query("RETURN [] as x", Collections.emptyMap());
        assertThat(result).hasSize(1).first().satisfies(row -> {
            Object x = row.get("x");
            assertThat(x).isInstanceOf(Void[].class);
            assertThat(((Void[]) x)).isEmpty();
        });
    }

    @Test
    public void emptyArray2Level() {

        Result result = session.query("RETURN [[]] as x", Collections.emptyMap());
        assertThat(result).hasSize(1).first().satisfies(row -> {
            Object x = row.get("x");
            assertThat(x).isInstanceOf(Void[].class);
            assertThat(((Void[]) x)).isEmpty();
        });
    }

    @Test
    public void empty2Dim1Level() {

        Result result = session.query("RETURN [[], []] as x", Collections.emptyMap());
        assertThat(result).hasSize(1).first().satisfies(row -> {
            Object x = row.get("x");
            assertThat(x).isInstanceOf(Void[].class);
            assertThat(((Void[]) x)).isEmpty();
        });
    }

    @Test // GH-902
    @SuppressWarnings("unchecked")
    public void emptyCollectedNodeList() {

        Result result = session.query("MATCH p=(:AAA)-[*]->(:BBB) RETURN COLLECT (DISTINCT nodes(p)) AS paths",
            Collections.emptyMap());
        assertThat(result).hasSize(1).first().satisfies(row -> {
            Object nestedLists = row.get("paths");
            assertThat(nestedLists).isInstanceOf(Void[].class);
            assertThat(((Void[]) nestedLists)).isEmpty();
        });
    }
}
