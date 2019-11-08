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
package org.neo4j.ogm.cypher.compiler;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.context.EntityGraphMapper;
import org.neo4j.ogm.context.EntityMapper;
import org.neo4j.ogm.context.MappedRelationship;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.domain.education.Course;
import org.neo4j.ogm.domain.education.School;
import org.neo4j.ogm.domain.education.Student;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.domain.forum.Forum;
import org.neo4j.ogm.domain.forum.ForumTopicLink;
import org.neo4j.ogm.domain.forum.Topic;
import org.neo4j.ogm.domain.gh609.CyclicNodeType;
import org.neo4j.ogm.domain.gh609.RefField;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.restaurant.Branch;
import org.neo4j.ogm.domain.restaurant.Franchise;
import org.neo4j.ogm.domain.restaurant.Location;
import org.neo4j.ogm.domain.restaurant.Restaurant;
import org.neo4j.ogm.domain.social.Individual;
import org.neo4j.ogm.domain.social.Mortal;
import org.neo4j.ogm.domain.travel.Person;
import org.neo4j.ogm.domain.travel.Place;
import org.neo4j.ogm.domain.travel.Visit;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.session.request.RowStatementFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class CompilerTest {

    private static MetaData mappingMetadata;
    private static MappingContext mappingContext;

    @BeforeClass
    public static void setUpTestDatabase() {
        mappingMetadata = new MetaData(
            "org.neo4j.ogm.domain.education",
            "org.neo4j.ogm.domain.forum",
            "org.neo4j.ogm.domain.social",
            "org.neo4j.domain.policy",
            "org.neo4j.ogm.domain.music",
            "org.neo4j.ogm.domain.restaurant",
            "org.neo4j.ogm.domain.travel");

        mappingContext = new MappingContext(mappingMetadata);
    }

    @Before
    public void setUpMapper() {
        mappingContext = new MappingContext(mappingMetadata);
    }

    @After
    public void cleanGraph() {
        mappingContext.clear();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnAttemptToMapNullObjectToCypherQuery() {
        new EntityGraphMapper(mappingMetadata, mappingContext).map(null);
    }

    @Test
    public void createSingleObjectWithLabelsAndProperties() {

        Student newStudent = new Student("Gary");
        assertThat(newStudent.getId()).isNull();
        Compiler compiler = mapAndCompile(newStudent, -1);
        assertThat(compiler.hasStatementsDependentOnNewNodes()).isFalse();
        assertThat(compiler.createNodesStatements()).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row CREATE (n:`DomainObject`:`Student`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type"
        );
    }

    @Test
    public void createSingleStatementForLabelsInDifferentOrder() throws Exception {
        Franchise franchise = new Franchise();

        Restaurant r1 = new Restaurant();
        r1.setName("La Strada Tooting");
        r1.labels = Arrays.asList("Delicious", "Foreign");

        Restaurant r2 = new Restaurant();
        r2.setName("La Strada Brno");
        r2.labels = Arrays.asList("Foreign", "Delicious");

        franchise.addBranch(new Branch(new Location(0.0, 0.0), franchise, r1));
        franchise.addBranch(new Branch(new Location(0.0, 0.0), franchise, r2));

        Compiler compiler = mapAndCompile(franchise, -1);
        assertThat(compiler.createNodesStatements()).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row CREATE (n:`Franchise`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type",
            // the order of labels here does not matter
            // the point is only one query for this combination of labels
            "UNWIND $rows as row CREATE (n:`Delicious`:`Foreign`:`Restaurant`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type"
        );
    }

    @Test
    public void updateSingleObjectPropertyAndLabel() {

        Student sheila = new Student("Sheila Smythe");
        Long sid = 0L;
        sheila.setId(sid);

        mappingContext.addNodeEntity(sheila);

        // now update the object's properties locally
        sheila.setName("Sheila Smythe-Jones");

        Compiler compiler = mapAndCompile(sheila, -1);
        assertThat(compiler.hasStatementsDependentOnNewNodes()).isFalse();
        compiler.useStatementFactory(new RowStatementFactory());
        assertThat(compiler.createNodesStatements()).isEmpty();
        assertThat(compiler.updateNodesStatements()).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (n) WHERE ID(n)=row.nodeId SET n:`DomainObject`:`Student` SET n += row.props RETURN row.nodeId as ref, ID(n) as id, $type as type"
        );
    }

    @Test
    public void doNothingIfNothingHasChanged() {

        Long existingNodeId = 0L;
        Student sheila = new Student();
        sheila.setId(existingNodeId);
        sheila.setName("Sheila Smythe");
        mappingContext.addNodeEntity(sheila);

        Compiler compiler = mapAndCompile(sheila, -1);
        compiler.useStatementFactory(new RowStatementFactory());
        assertThat(compiler.createNodesStatements()).isEmpty();
        assertThat(compiler.updateNodesStatements()).isEmpty();
    }

    @Test
    public void createSimpleRelationshipsBetweenObjects() {

        School waller = new School("Waller");
        Teacher mary = new Teacher("Mary");

        mary.setSchool(waller);
        waller.getTeachers().add(mary);

        Compiler compiler = mapAndCompile(waller, -1);
        compiler.useStatementFactory(new RowStatementFactory());
        assertThat(compiler.hasStatementsDependentOnNewNodes()).isTrue();

        assertThat(compiler.createNodesStatements()).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row CREATE (n:`DomainObject`:`School`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type",
            "UNWIND $rows as row CREATE (n:`Teacher`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type"
        );

        assertThat(compiler.createRelationshipsStatements()).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`TEACHERS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, $type as type",
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`SCHOOL`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, $type as type"
        );
    }

    @Test
    public void expectNoChangesWhenDomainUnchanged() {

        // create
        Long wallerId = 0L;
        Long maryId = 1L;

        School waller = new School("Waller");
        waller.setId(wallerId);

        Teacher mary = new Teacher("Mary");
        mary.setId(maryId);

        // relate
        mary.setSchool(waller);

        // validate the domain model
        assertThat(mary.getSchool().equals(waller)).isTrue();
        assertThat(waller.getTeachers().contains(mary)).isTrue();
        assertThat(waller.getTeachers().size() == 1).isTrue();

        // set the mapping context accordingly
        mappingContext.addNodeEntity(mary);
        mappingContext.addNodeEntity(waller);

        mappingContext.addRelationship(new MappedRelationship(maryId, "SCHOOL", wallerId, Teacher.class, School.class));
        mappingContext
            .addRelationship(new MappedRelationship(wallerId, "TEACHERS", maryId, School.class, Teacher.class));

        Compiler compiler = mapAndCompile(waller, -1);
        compiler.useStatementFactory(new RowStatementFactory());

        assertThat(compiler.createNodesStatements()).isEmpty();
        assertThat(compiler.updateNodesStatements()).isEmpty();
        assertThat(compiler.createRelationshipsStatements()).isEmpty();
        assertThat(compiler.updateRelationshipStatements()).isEmpty();

        compiler = mapAndCompile(mary, -1);
        assertThat(compiler.createNodesStatements()).isEmpty();
        assertThat(compiler.updateNodesStatements()).isEmpty();
        assertThat(compiler.createRelationshipsStatements()).isEmpty();
        assertThat(compiler.updateRelationshipStatements()).isEmpty();
    }

    @Test
    public void addObjectToExistingCollection() {

        // create
        Long wallerId = 0L;
        Long maryId = 1L;

        School waller = new School("Waller");
        waller.setId(wallerId);

        Teacher mary = new Teacher("Mary");
        mary.setId(maryId);

        // relate
        mary.setSchool(waller);

        // validate the domain model
        assertThat(mary.getSchool()).isEqualTo(waller);
        assertThat(waller.getTeachers()).contains(mary);
        assertThat(waller.getTeachers()).hasSize(1);

        // set the mapping context accordingly
        mappingContext.addNodeEntity(mary);
        mappingContext.addNodeEntity(waller);
        mappingContext.addRelationship(new MappedRelationship(maryId, "SCHOOL", wallerId, Teacher.class, School.class));
        mappingContext
            .addRelationship(new MappedRelationship(wallerId, "TEACHERS", maryId, School.class, Teacher.class));

        Teacher jim = new Teacher("Jim");
        jim.setSchool(waller);

        assertThat(waller.getTeachers()).contains(jim);
        assertThat(waller.getTeachers()).hasSize(2);
        assertThat(jim.getSchool()).isEqualTo(waller);

        //Save jim
        Compiler compiler = mapAndCompile(jim, -1);

        List<Statement> createNodesStatements = compiler.createNodesStatements();
        assertThat(createNodesStatements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row CREATE (n:`Teacher`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type"
        );

        assertThat(createNodesStatements).extracting(Statement::getParameters);
        for (Statement statement : createNodesStatements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(1);
        }

        List<Statement> createRelsStatements = compiler.createRelationshipsStatements();
        assertThat(createRelsStatements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`SCHOOL`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, $type as type",
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`TEACHERS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, $type as type"
        );

        //Save waller
        compiler = mapAndCompile(waller, -1);

        createNodesStatements = compiler.createNodesStatements();
        assertThat(createNodesStatements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row CREATE (n:`Teacher`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type"
        );
        for (Statement statement : createNodesStatements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(1);
        }

        createRelsStatements = compiler.createRelationshipsStatements();
        assertThat(createRelsStatements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`SCHOOL`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, $type as type",
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`TEACHERS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, $type as type"
        );

        //Save mary
        compiler = mapAndCompile(mary, -1);

        createNodesStatements = compiler.createNodesStatements();
        assertThat(createNodesStatements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row CREATE (n:`Teacher`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type"
        );
        for (Statement statement : createNodesStatements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(1);
        }

        createRelsStatements = compiler.createRelationshipsStatements();
        assertThat(createRelsStatements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`SCHOOL`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, $type as type",
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`TEACHERS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, $type as type"
        );
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
        //Save teacher
        Compiler compiler = mapAndCompile(teacher, -1);

        List<Statement> createNodesStatements = compiler.createNodesStatements();
        assertThat(createNodesStatements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row CREATE (n:`Teacher`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type",
            "UNWIND $rows as row CREATE (n:`Course`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type",
            "UNWIND $rows as row CREATE (n:`DomainObject`:`Student`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type"
        );

        for (Statement statement : createNodesStatements) {
            List rows = (List) statement.getParameters().get("rows");
            if (statement.getStatement().contains("Teacher")) {
                assertThat(rows).hasSize(1);
            }
            if (statement.getStatement().contains("Student")) {
                assertThat(rows).hasSize(3);
            }
            if (statement.getStatement().contains("Course")) {
                assertThat(rows).hasSize(2);
            }
        }

        List<Statement> createRelsStatements = compiler.createRelationshipsStatements();
        assertThat(createRelsStatements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`COURSES`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, $type as type",
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`STUDENTS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, $type as type"
        );
        for (Statement statement : createRelsStatements) {
            List rows = (List) statement.getParameters().get("rows");
            if (statement.getStatement().contains("STUDENTS")) {
                assertThat(rows).hasSize(4);
            }
            if (statement.getStatement().contains("COURSES")) {
                assertThat(rows).hasSize(2);
            }
        }
    }

    @Test
    public void shouldCorrectlyRemoveRelationshipWhenItemIsRemovedFromCollection() {

        // simple music course with three students

        Long mid = 0L;

        Long xid = 1L;
        Long yid = 2L;
        Long zid = 3L;

        Course music = new Course("GCSE Music");
        music.setId(mid);

        Student xavier = new Student("xavier");
        xavier.setId(xid);

        Student yvonne = new Student("Yvonne");
        yvonne.setId(yid);

        Student zack = new Student("Zack");
        zack.setId(zid);

        music.setStudents(Arrays.asList(yvonne, xavier, zack));

        mappingContext.addRelationship(new MappedRelationship(mid, "STUDENTS", xid, Course.class, Student.class));
        mappingContext.addRelationship(new MappedRelationship(mid, "STUDENTS", yid, Course.class, Student.class));
        mappingContext.addRelationship(new MappedRelationship(mid, "STUDENTS", zid, Course.class, Student.class));

        mappingContext.addNodeEntity(xavier);
        mappingContext.addNodeEntity(yvonne);
        mappingContext.addNodeEntity(zack);
        mappingContext.addNodeEntity(music);

        // now, update the domain model, setting yvonne as the only music student (i.e remove zack and xavier)
        music.setStudents(Arrays.asList(yvonne));

        //Save music
        Compiler compiler = mapAndCompile(music, -1);

        assertThat(compiler.createNodesStatements()).isEmpty();

        assertThat(compiler.createRelationshipsStatements()).isEmpty();

        List<Statement> deleteRelsStatement = compiler.deleteRelationshipStatements();
        assertThat(deleteRelsStatement).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`STUDENTS`]->(endNode) DELETE rel"
        );
        assertThat(((List) deleteRelsStatement.get(0).getParameters().get("rows"))).hasSize(2);
    }

    @Test
    public void shouldCorrectlyRemoveRelationshipWhenItemIsMovedToDifferentCollection() {

        Long teacherId = 0L;
        Long businessStudiesCourseId = 1L;
        Long designTechnologyCourseId = 2L;
        Long shivaniId = 3L;

        Course designTech = new Course("GCSE Design & Technology");
        designTech.setId(designTechnologyCourseId);

        Course businessStudies = new Course("GNVQ Business Studies");
        businessStudies.setId(businessStudiesCourseId);

        Teacher msThompson = new Teacher();
        msThompson.setId(teacherId);
        msThompson.setName("Ms Thompson");
        msThompson.setCourses(Arrays.asList(businessStudies, designTech));

        Student shivani = new Student("Shivani");
        shivani.setId(shivaniId);

        mappingContext.addNodeEntity(msThompson);
        mappingContext.addNodeEntity(businessStudies);
        mappingContext.addNodeEntity(designTech);
        mappingContext.addNodeEntity(shivani);

        mappingContext.addRelationship(
            new MappedRelationship(teacherId, "COURSES", businessStudiesCourseId, Teacher.class, Course.class));
        mappingContext.addRelationship(
            new MappedRelationship(teacherId, "COURSES", designTechnologyCourseId, Teacher.class, Course.class));
        mappingContext.addRelationship(
            new MappedRelationship(businessStudiesCourseId, "STUDENTS", shivaniId, Teacher.class, Student.class));

        // move shivani from one course to the other
        businessStudies.setStudents(Collections.emptyList());
        designTech.setStudents(Arrays.asList(shivani));

        //Save msThomson
        // we expect a new relationship to be created, and an old one deleted
        Compiler compiler = mapAndCompile(msThompson, -1);

        List<Statement> statements = compiler.createNodesStatements();
        assertThat(statements).isEmpty();

        statements = compiler.createRelationshipsStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`STUDENTS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, $type as type"
        );
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(1);
        }

        List<Statement> deleteRelsStatements = compiler.deleteRelationshipStatements();
        assertThat(deleteRelsStatements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`STUDENTS`]->(endNode) DELETE rel"
        );
        assertThat(((List) deleteRelsStatements.get(0).getParameters().get("rows"))).hasSize(1);

        // fixme: these other tests now need to be in their own test method, because
        // a bug fix to the deletion code means that a second deletion won't (correctly) fire again
        // expect a delete, but don't expect the new relationship to be created, because the fact of it
        // is inaccessible from the businessStudies object
        //        expectOnSave(businessStudies,
        //                "MATCH ($1)-[_0:STUDENTS]->($3) WHERE id($1)=1 AND id($3)=3 DELETE _0");
        //
        //        // expect the new relationship, but don't expect the old one to be deleted, because the fact
        //        // of it is inaccessible from the designTech object
        //        expectOnSave(designTech,
        //                "MATCH ($2) WHERE id($2)=2 MATCH ($3) WHERE id($3)=3 MERGE ($2)-[_0:`STUDENTS`]->($3) RETURN id(_0) AS _0");
        //
        //        // we can't explore the object model from shivani at all, so no changes.
        //        expectOnSave(shivani, "");

    }

    @Test
    public void shouldCorrectlyRemoveRelationshipWhenItemIsDisconnectedFromNonOwningSide() {

        Long schoolId = 0L;
        Long whiteId = 1L;
        Long jonesId = 2L;

        School hillsRoad = new School("Hills Road Sixth Form College");
        hillsRoad.setId(schoolId);

        Teacher mrWhite = new Teacher("Mr White");
        mrWhite.setId(whiteId);

        Teacher missJones = new Teacher("Miss Jones");
        missJones.setId(jonesId);

        hillsRoad.setTeachers(Arrays.asList(missJones, mrWhite));
        assertThat(hillsRoad.getTeachers()).contains(mrWhite);
        assertThat(hillsRoad.getTeachers()).contains(missJones);
        assertThat(mrWhite.getSchool()).isEqualTo(hillsRoad);
        assertThat(missJones.getSchool()).isEqualTo(hillsRoad);

        mappingContext.addNodeEntity(hillsRoad);
        mappingContext.addNodeEntity(mrWhite);
        mappingContext.addNodeEntity(missJones);

        mappingContext
            .addRelationship(new MappedRelationship(schoolId, "TEACHERS", whiteId, School.class, Teacher.class));
        mappingContext
            .addRelationship(new MappedRelationship(schoolId, "TEACHERS", jonesId, School.class, Teacher.class));
        mappingContext
            .addRelationship(new MappedRelationship(whiteId, "SCHOOL", schoolId, Teacher.class, School.class));
        mappingContext
            .addRelationship(new MappedRelationship(jonesId, "SCHOOL", schoolId, Teacher.class, School.class));

        // Fire Mr White:
        mrWhite.setSchool(null);

        // validate model:
        assertThat(mrWhite.getSchool()).isNull();
        assertThat(hillsRoad.getTeachers()).doesNotContain(mrWhite);

        // we expect hillsRoad relationship to mrWhite to be removed.
        // however, the change to MrWhite's relationship is not detected.
        // this is because MrWhite is not "visited" during the traversal of
        // hillsRoad - his reference is now inaccessible. this looks like a FIXME

        Compiler compiler = mapAndCompile(hillsRoad, -1);

        List<Statement> statements = compiler.createNodesStatements();
        assertThat(statements).isEmpty();

        statements = compiler.createRelationshipsStatements();
        assertThat(statements).isEmpty();

        statements = compiler.deleteRelationshipStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`TEACHERS`]->(endNode) DELETE rel"
        );
        assertThat(((List) statements.get(0).getParameters().get("rows"))).hasSize(1);

        // we expect mrWhite's relationship to hillsRoad to be removed
        // but the change to hillsRoad's relationship with MrWhite is not detected
        // this is because hillsRoad object is no longer directly accessible from MrWhite
        // looks like a FIXME (infer symmetric deletions)
        compiler = mapAndCompile(mrWhite, -1);

        statements = compiler.createNodesStatements();
        assertThat(statements).isEmpty();

        statements = compiler.createRelationshipsStatements();
        assertThat(statements).isEmpty();

        statements = compiler.deleteRelationshipStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`SCHOOL`]->(endNode) DELETE rel"
        );
        assertThat(((List) statements.get(0).getParameters().get("rows"))).hasSize(1);

        // because missJones has a reference to hillsRoad, we expect an outcome
        // the same as if we had saved hillsRoiad directly.
        //expectOnSave(missJones,
        //        "MATCH ($0)-[_2:TEACHERS]->($1) WHERE id($0)=0 AND id($1)=1 DELETE _2");
    }

    @Test
    public void shouldCreateRelationshipWithPropertiesFromRelationshipEntity() {

        Forum forum = new Forum();
        forum.setName("SDN FAQs");

        Topic topic = new Topic();

        ForumTopicLink link = new ForumTopicLink();
        link.setForum(forum);
        link.setTopic(topic);
        link.setTimestamp(1647209L);

        forum.setTopicsInForum(Arrays.asList(link));

        // the entire object tree is accessible from the forum
        // Note that a relationshipEntity has a direction by default (srcNode -> tgtNode)
        // because it has an annotation, so we should not create an inverse relationship.
        Compiler compiler = mapAndCompile(forum, -1);

        List<Statement> statements = compiler.createNodesStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row CREATE (n:`Forum`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type",
            "UNWIND $rows as row CREATE (n:`Topic`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type"
        );
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(1);
        }

        statements = compiler.createRelationshipsStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId CREATE (startNode)-[rel:`HAS_TOPIC`]->(endNode) SET rel += row.props RETURN row.relRef as ref, ID(rel) as id, $type as type"
        );
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(1);
        }

        // the entire object tree is accessible from the link
        compiler = mapAndCompile(link, -1);

        statements = compiler.createNodesStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row CREATE (n:`Forum`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type",
            "UNWIND $rows as row CREATE (n:`Topic`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type"
        );
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(1);
        }

        statements = compiler.createRelationshipsStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId CREATE (startNode)-[rel:`HAS_TOPIC`]->(endNode) SET rel += row.props RETURN row.relRef as ref, ID(rel) as id, $type as type"
        );
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(1);
        }

        // the related entity is not visible from the Topic object.
        compiler = mapAndCompile(topic, -1);

        statements = compiler.createNodesStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row CREATE (n:`Topic`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type"
        );
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(1);
        }

        statements = compiler.createRelationshipsStatements();
        assertThat(statements).isEmpty();
    }

    @Test
    public void shouldMergeNewRelationshipEntity() throws Exception {
        Person frantisek = new Person("Frantisek");
        Place scotland = new Place("Scotland");
        Visit visit = frantisek.addVisit(scotland, "Holiday");

        Compiler compiler = mapAndCompile(frantisek, -1);

        List<Statement> statements = compiler.createRelationshipsStatements();

        assertThat(statements)
            .extracting(Statement::getStatement)
            .containsOnly("UNWIND $rows as row "
                + "MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode "
                + "MATCH (endNode) WHERE ID(endNode) = row.endNodeId "
                + "MERGE (startNode)-[rel:`VISITED` {`identifier`: row.props.`identifier`}]->(endNode) "
                + "SET rel += row.props "
                + "RETURN row.relRef as ref, ID(rel) as id, $type as type");
    }

    @Test
    public void shouldUpdatingExistingRelationshipEntity() {

        Long forumId = 0L;
        Long topicId = 1L;
        Long relationshipId = 2L;

        Forum forum = new Forum();
        forum.setId(forumId);
        forum.setName("Spring Data Neo4j");

        Topic topic = new Topic();
        topic.setTopicId(topicId);
        topic.setInActive(Boolean.FALSE);

        ForumTopicLink link = new ForumTopicLink();
        link.setId(relationshipId);
        link.setForum(forum);
        link.setTopic(topic);

        forum.setTopicsInForum(Arrays.asList(link));

        mappingContext.addNodeEntity(forum);
        mappingContext.addNodeEntity(topic);
        mappingContext.addRelationshipEntity(link, relationshipId);
        MappedRelationship mappedRelationship = new MappedRelationship(forumId, "HAS_TOPIC", topicId, relationshipId,
            Forum.class, ForumTopicLink.class);
        mappingContext.addRelationship(mappedRelationship);

        // change the timestamp
        link.setTimestamp(327790L);

        // expect the property on the relationship entity to be updated on the graph relationship
        Compiler compiler = mapAndCompile(link, -1);

        List<Statement> statements = compiler.createNodesStatements();
        assertThat(statements).isEmpty();
        statements = compiler.updateRelationshipStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows AS row MATCH ()-[r]->() WHERE ID(r) = row.relId SET r += row.props RETURN ID(r) as ref, ID(r) as id, $type as type"
        );
        assertThat((List) statements.get(0).getParameters().get("rows")).hasSize(1);
    }

    @Test
    public void shouldDeleteExistingRelationshipEntity() {

        Long forumId = 0L;
        Long topicId = 1L;
        Long linkId = 2L;

        Forum forum = new Forum();
        forum.setId(forumId);
        forum.setName("Spring Data Neo4j");

        Topic topic = new Topic();
        topic.setTopicId(topicId);
        topic.setInActive(Boolean.FALSE);

        ForumTopicLink link = new ForumTopicLink();
        link.setId(linkId);
        link.setForum(forum);
        link.setTopic(topic);

        forum.setTopicsInForum(Arrays.asList(link));

        mappingContext.addNodeEntity(forum);
        mappingContext.addNodeEntity(topic);
        mappingContext.addRelationshipEntity(link, linkId);

        // the mapping context remembers the relationship between the forum and the topic in the graph
        mappingContext
            .addRelationship(new MappedRelationship(forumId, "HAS_TOPIC", topicId, Forum.class, ForumTopicLink.class));

        // unlink the objects manually
        forum.setTopicsInForum(null);
        link.setTopic(null);

        // expect the delete to be recognised when the forum is saved
        Compiler compiler = mapAndCompile(forum, -1);

        List<Statement> statements = compiler.createRelationshipsStatements();
        assertThat(statements).isEmpty();

        statements = compiler.deleteRelationshipStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`HAS_TOPIC`]->(endNode) DELETE rel"
        );
        assertThat(((List) statements.get(0).getParameters().get("rows"))).hasSize(1);

        // expect the delete to be recognised if the RE is saved
        //        expectOnSave(link, "MATCH ($0)-[_0:HAS_TOPIC]->($1) WHERE id($0)=0 AND id($1)=1 DELETE _0");
        //
        //        // expect nothing to happen if the topic is saved, because the domain model does not
        //        // permit navigation from the topic to the RE (topic has no reference to it)
        //        expectOnSave(topic, "");

        // todo: more tests re saving deletes from REs marked as incoming relationships

    }

    @Test // DATAGRAPH-589
    public void createSimpleRelationshipWithIllegalCharactersBetweenObjects() {

        Artist theBeatles = new Artist("The Beatles");
        Album please = new Album("Please Please Me");
        theBeatles.getAlbums().add(please);
        please.setArtist(theBeatles);

        Compiler compiler = mapAndCompile(theBeatles, -1);

        List<Statement> statements = compiler.createNodesStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row CREATE (n:`l'artiste`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type",
            "UNWIND $rows as row CREATE (n:`l'album`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type"
        );
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(1);
        }

        statements = compiler.createRelationshipsStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`HAS-ALBUM`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, $type as type"
        );
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(1);
        }
    }

    @Test // DATAGRAPH-594
    public void createOutgoingRelationWhenUnmarkedRelationIsSpecified() {

        Individual adam = new Individual();
        adam.setName("Adam");

        Individual vince = new Individual();
        vince.setName("Vince");

        adam.setFriends(Collections.singletonList(vince));

        Compiler compiler = mapAndCompile(adam, -1);

        List<Statement> statements = compiler.createNodesStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row CREATE (n:`Individual`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type"
        );
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(2);
        }

        statements = compiler.createRelationshipsStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`FRIENDS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, $type as type"
        );
        List rows = (List) statements.get(0).getParameters().get("rows");
        assertThat(rows).hasSize(1);

        Map row = (Map) rows.get(0);
        assertThat(row.get("startNodeId")).isEqualTo(mappingContext.nativeId(adam));
        assertThat(row.get("endNodeId")).isEqualTo(mappingContext.nativeId(vince));
    }

    @Test // DATAGRAPH-594
    public void createIncomingRelationWhenSpecified() {
        Mortal adam = new Mortal("Adam");
        Mortal vince = new Mortal("Vince");

        adam.getKnownBy().add(vince);

        Compiler compiler = mapAndCompile(adam, -1);

        List<Statement> statements = compiler.createNodesStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row CREATE (n:`Mortal`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type"
        );
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(2);
        }

        statements = compiler.createRelationshipsStatements();
        assertThat(statements).extracting(Statement::getStatement).containsOnly(
            "UNWIND $rows as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`KNOWN_BY`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, $type as type"
        );
        List rows = (List) statements.get(0).getParameters().get("rows");
        assertThat(rows).hasSize(1);

        Map row = (Map) rows.get(0);
        assertThat(row.get("startNodeId")).isEqualTo(mappingContext.nativeId(vince));
        assertThat(row.get("endNodeId")).isEqualTo(mappingContext.nativeId(adam));
    }

    private static Compiler mapAndCompile(Object object, int depth) {
        EntityMapper mapper = new EntityGraphMapper(mappingMetadata, mappingContext);
        CompileContext context = mapper.map(object, depth);
        Compiler compiler = context.getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());
        return compiler;
    }
}
