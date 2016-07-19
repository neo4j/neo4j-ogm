/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.cypher;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.utils.MetaData;
import org.neo4j.ogm.compiler.CompileContext;
import org.neo4j.ogm.compiler.Compiler;
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
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.social.Individual;
import org.neo4j.ogm.domain.social.Mortal;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.session.request.RowStatementFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class CypherCompilerTest {

    private static MetaData mappingMetadata;
    private static MappingContext mappingContext;
    private EntityMapper mapper;

    @BeforeClass
    public static void setUpTestDatabase() {
        mappingMetadata = new MetaData("org.neo4j.ogm.domain.education", "org.neo4j.ogm.domain.forum", "org.neo4j.ogm.domain.social", "org.neo4j.domain.policy", "org.neo4j.ogm.domain.music");
        mappingContext = new MappingContext(mappingMetadata);
    }

    @Before
    public void setUpMapper() {
        mappingContext = new MappingContext(mappingMetadata);
        this.mapper = new EntityGraphMapper(mappingMetadata, mappingContext);
    }

    @After
    public void cleanGraph() {
        mappingContext.clear();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnAttemptToMapNullObjectToCypherQuery() {
        this.mapper.map(null);
    }

    @Test
    public void createSingleObjectWithLabelsAndProperties() {

        Student newStudent = new Student("Gary");
        assertNull(newStudent.getId());
        Compiler compiler = mapAndCompile(newStudent);
        assertFalse(compiler.hasStatementsDependentOnNewNodes());
        assertEquals("UNWIND {rows} as row CREATE (n:`Student`:`DomainObject`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type",
                compiler.createNodesStatements().get(0).getStatement());
    }

    @Test
    public void updateSingleObjectPropertyAndLabel() {

        Student sheila = new Student("Sheila Smythe");
        Long sid = 0L;
        sheila.setId(sid);

        mappingContext.registerNodeEntity(sheila, sid);

        // now update the object's properties locally
        sheila.setName("Sheila Smythe-Jones");

        Compiler compiler = mapAndCompile(sheila);
        assertFalse(compiler.hasStatementsDependentOnNewNodes());
        compiler.useStatementFactory(new RowStatementFactory());
        assertEquals(0, compiler.createNodesStatements().size());
        assertEquals("UNWIND {rows} as row MATCH (n) WHERE ID(n)=row.nodeId SET n:`Student`:`DomainObject` SET n += row.props RETURN row.nodeId as ref, ID(n) as id, row.type as type",
                compiler.updateNodesStatements().get(0).getStatement());

    }

    @Test
    public void doNothingIfNothingHasChanged() {

        Long existingNodeId = 0L;
        Student sheila = new Student();
        sheila.setId(existingNodeId);
        sheila.setName("Sheila Smythe");
        mappingContext.registerNodeEntity(sheila, existingNodeId);

        Compiler compiler = mapAndCompile(sheila);
        compiler.useStatementFactory(new RowStatementFactory());
        assertEquals(0, compiler.createNodesStatements().size());
        assertEquals(0, compiler.updateNodesStatements().size());
    }

    @Test
    public void createSimpleRelationshipsBetweenObjects() {

        School waller = new School("Waller");
        Teacher mary = new Teacher("Mary");

        mary.setSchool(waller);
        waller.getTeachers().add(mary);


        Compiler compiler = mapAndCompile(waller);
        compiler.useStatementFactory(new RowStatementFactory());
        assertTrue(compiler.hasStatementsDependentOnNewNodes());

        List<String> createStatements = cypherStatements(compiler.createNodesStatements());
        assertEquals(2, createStatements.size());
        assertTrue(createStatements.contains("UNWIND {rows} as row CREATE (n:`School`:`DomainObject`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        assertTrue(createStatements.contains("UNWIND {rows} as row CREATE (n:`Teacher`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        List<String> createRelStatements = cypherStatements(compiler.createRelationshipsStatements());
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`TEACHERS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`SCHOOL`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));
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
        assertTrue(mary.getSchool().equals(waller));
        assertTrue(waller.getTeachers().contains(mary));
        assertTrue(waller.getTeachers().size() == 1);

        // set the mapping context accordingly
        mappingContext.registerNodeEntity(mary, maryId);
        mappingContext.registerNodeEntity(waller, wallerId);

        mappingContext.registerRelationship(new MappedRelationship(maryId, "SCHOOL", wallerId, Teacher.class, School.class));
        mappingContext.registerRelationship(new MappedRelationship(wallerId, "TEACHERS", maryId, School.class, Teacher.class));

        Compiler compiler = mapAndCompile(waller);
        compiler.useStatementFactory(new RowStatementFactory());

        assertEquals(0, compiler.createNodesStatements().size());
        assertEquals(0, compiler.updateNodesStatements().size());
        assertEquals(0, compiler.createRelationshipsStatements().size());
        assertEquals(0, compiler.updateRelationshipStatements().size());

        compiler = mapAndCompile(mary);
        assertEquals(0, compiler.createNodesStatements().size());
        assertEquals(0, compiler.updateNodesStatements().size());
        assertEquals(0, compiler.createRelationshipsStatements().size());
        assertEquals(0, compiler.updateRelationshipStatements().size());

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
        assertTrue(mary.getSchool().equals(waller));
        assertTrue(waller.getTeachers().contains(mary));
        assertTrue(waller.getTeachers().size() == 1);

        // set the mapping context accordingly
        mappingContext.registerNodeEntity(mary, maryId);
        mappingContext.registerNodeEntity(waller, wallerId);
        mappingContext.registerRelationship(new MappedRelationship(maryId, "SCHOOL", wallerId, Teacher.class, School.class));
        mappingContext.registerRelationship(new MappedRelationship(wallerId, "TEACHERS", maryId, School.class, Teacher.class));

        Teacher jim = new Teacher("Jim");
        jim.setSchool(waller);

        assertTrue(waller.getTeachers().contains(jim));
        assertTrue(waller.getTeachers().size() == 2);
        assertTrue(jim.getSchool().equals(waller));

        //Save jim
        Compiler compiler = mapper.map(jim).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.createNodesStatements();
        List<String> createNodeStatements = cypherStatements(statements);
        assertEquals(1, createNodeStatements.size());
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`Teacher`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(1, rows.size());
        }

        statements = compiler.createRelationshipsStatements();
        List<String> createRelStatements = cypherStatements(statements);
        assertEquals(2, createRelStatements.size());
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`SCHOOL`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`TEACHERS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));

        //Save waller
        compiler = mapper.map(waller).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        statements = compiler.createNodesStatements();
        createNodeStatements = cypherStatements(statements);
        assertEquals(1, createNodeStatements.size());
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`Teacher`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(1, rows.size());
        }

        statements = compiler.createRelationshipsStatements();
        createRelStatements = cypherStatements(statements);
        assertEquals(2, createRelStatements.size());
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`SCHOOL`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`TEACHERS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));


        //Save mary
        compiler = mapper.map(mary).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        statements = compiler.createNodesStatements();
        createNodeStatements = cypherStatements(statements);
        assertEquals(1, createNodeStatements.size());
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`Teacher`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(1, rows.size());
        }

        statements = compiler.createRelationshipsStatements();
        createRelStatements = cypherStatements(statements);
        assertEquals(2, createRelStatements.size());
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`SCHOOL`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`TEACHERS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));
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
        Compiler compiler = mapper.map(teacher).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.createNodesStatements();
        List<String> createNodeStatements = cypherStatements(statements);
        assertEquals(3, createNodeStatements.size());
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`Teacher`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`Course`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`Student`:`DomainObject`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            if(statement.getStatement().contains("Teacher")) {
                assertEquals(1, rows.size());
            }
            if(statement.getStatement().contains("Student")) {
                assertEquals(3, rows.size());
            }
            if(statement.getStatement().contains("Course")) {
                assertEquals(2, rows.size());
            }
        }

        statements = compiler.createRelationshipsStatements();
        List<String> createRelStatements = cypherStatements(statements);
        assertEquals(2, createRelStatements.size());
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`COURSES`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`STUDENTS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            if(statement.getStatement().contains("STUDENTS")) {
                assertEquals(4, rows.size());
            }
            if(statement.getStatement().contains("COURSES")) {
                assertEquals(2, rows.size());
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

        mappingContext.registerRelationship(new MappedRelationship(mid, "STUDENTS", xid, Course.class, Student.class));
        mappingContext.registerRelationship(new MappedRelationship(mid, "STUDENTS", yid, Course.class, Student.class));
        mappingContext.registerRelationship(new MappedRelationship(mid, "STUDENTS", zid, Course.class, Student.class));

        mappingContext.registerNodeEntity(xavier, xid);
        mappingContext.registerNodeEntity(yvonne, yid);
        mappingContext.registerNodeEntity(zack, zid);
        mappingContext.registerNodeEntity(music, mid);

        // now, update the domain model, setting yvonne as the only music student (i.e remove zack and xavier)
        music.setStudents(Arrays.asList(yvonne));

        //Save music
        Compiler compiler = mapper.map(music).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.createNodesStatements();
        assertEquals(0, statements.size());

        statements = compiler.createRelationshipsStatements();
        assertEquals(0, statements.size());

        statements = compiler.deleteRelationshipStatements();
        assertEquals(1, statements.size());
        assertEquals("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`STUDENTS`]->(endNode) DELETE rel",
                statements.get(0).getStatement());
        assertEquals(2, ((List)statements.get(0).getParameters().get("rows")).size());
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

        mappingContext.registerNodeEntity(msThompson, teacherId);
        mappingContext.registerNodeEntity(businessStudies, businessStudiesCourseId);
        mappingContext.registerNodeEntity(designTech, designTechnologyCourseId);
        mappingContext.registerNodeEntity(shivani, shivaniId);

        mappingContext.registerRelationship(new MappedRelationship(teacherId, "COURSES", businessStudiesCourseId, Teacher.class, Course.class));
        mappingContext.registerRelationship(new MappedRelationship(teacherId, "COURSES", designTechnologyCourseId, Teacher.class, Course.class));
        mappingContext.registerRelationship(new MappedRelationship(businessStudiesCourseId, "STUDENTS", shivaniId, Teacher.class, Student.class));

        // move shivani from one course to the other
        businessStudies.setStudents(Collections.<Student>emptyList());
        designTech.setStudents(Arrays.asList(shivani));

        //Save msThomson
        // we expect a new relationship to be created, and an old one deleted
        Compiler compiler = mapper.map(msThompson).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.createNodesStatements();
        assertEquals(0, statements.size());

        statements = compiler.createRelationshipsStatements();
        List<String> createRelStatements = cypherStatements(statements);
        assertEquals(1, createRelStatements.size());
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`STUDENTS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(1, rows.size());
        }

        statements = compiler.deleteRelationshipStatements();
        assertEquals(1, statements.size());
        assertEquals("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`STUDENTS`]->(endNode) DELETE rel",
                statements.get(0).getStatement());
        assertEquals(1, ((List)statements.get(0).getParameters().get("rows")).size());


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

        // need to ensure teachers list is mutable
        hillsRoad.setTeachers(new ArrayList<>(Arrays.asList(missJones, mrWhite)));
        assertTrue(hillsRoad.getTeachers().contains(mrWhite));
        assertTrue(hillsRoad.getTeachers().contains(missJones));
        Assert.assertEquals(hillsRoad, mrWhite.getSchool());
        Assert.assertEquals(hillsRoad, missJones.getSchool());


        mappingContext.registerNodeEntity(hillsRoad, schoolId);
        mappingContext.registerNodeEntity(mrWhite, whiteId);
        mappingContext.registerNodeEntity(missJones, jonesId);

        mappingContext.registerRelationship(new MappedRelationship(schoolId, "TEACHERS", whiteId, School.class, Teacher.class));
        mappingContext.registerRelationship(new MappedRelationship(schoolId, "TEACHERS", jonesId, School.class, Teacher.class));
        mappingContext.registerRelationship(new MappedRelationship(whiteId, "SCHOOL", schoolId, Teacher.class, School.class));
        mappingContext.registerRelationship(new MappedRelationship(jonesId, "SCHOOL", schoolId, Teacher.class, School.class));

        // Fire Mr White:
        mrWhite.setSchool(null);

        // validate model:
        assertNull(mrWhite.getSchool());
        assertFalse(hillsRoad.getTeachers().contains(mrWhite));

        // we expect hillsRoad relationship to mrWhite to be removed.
        // however, the change to MrWhite's relationship is not detected.
        // this is because MrWhite is not "visited" during the traversal of
        // hillsRoad - his reference is now inaccessible. this looks like a FIXME

        Compiler compiler = mapper.map(hillsRoad).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.createNodesStatements();
        assertEquals(0, statements.size());


        statements = compiler.createRelationshipsStatements();
        assertEquals(0, statements.size());

        statements = compiler.deleteRelationshipStatements();
        assertEquals(1, statements.size());
        assertEquals("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`TEACHERS`]->(endNode) DELETE rel",
                statements.get(0).getStatement());
        assertEquals(1, ((List)statements.get(0).getParameters().get("rows")).size());

        // we expect mrWhite's relationship to hillsRoad to be removed
        // but the change to hillsRoad's relationship with MrWhite is not detected
        // this is because hillsRoad object is no longer directly accessible from MrWhite
        // looks like a FIXME (infer symmetric deletions)
        compiler = mapper.map(mrWhite).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        statements = compiler.createNodesStatements();
        assertEquals(0, statements.size());


        statements = compiler.createRelationshipsStatements();
        assertEquals(0, statements.size());

        statements = compiler.deleteRelationshipStatements();
        assertEquals(1, statements.size());
        assertEquals("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`SCHOOL`]->(endNode) DELETE rel",
                statements.get(0).getStatement());
        assertEquals(1, ((List)statements.get(0).getParameters().get("rows")).size());

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
        Compiler compiler = mapper.map(forum).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.createNodesStatements();
        List<String> createNodeStatements = cypherStatements(statements);
        assertEquals(2, createNodeStatements.size());
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`Forum`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`Topic`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(1, rows.size());
        }

        statements = compiler.createRelationshipsStatements();
        List<String> createRelStatements = cypherStatements(statements);
        assertEquals(1, createRelStatements.size());
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`HAS_TOPIC`{ `timestamp`: row.props.timestamp}]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(1, rows.size());
        }

        // the entire object tree is accessible from the link
        compiler = mapper.map(link).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        statements = compiler.createNodesStatements();
        createNodeStatements = cypherStatements(statements);
        assertEquals(2, createNodeStatements.size());
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`Forum`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`Topic`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(1, rows.size());
        }

        statements = compiler.createRelationshipsStatements();
        createRelStatements = cypherStatements(statements);
        assertEquals(1, createRelStatements.size());
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`HAS_TOPIC`{ `timestamp`: row.props.timestamp}]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(1, rows.size());
        }

        // the related entity is not visible from the Topic object.
        compiler = mapper.map(topic).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        statements = compiler.createNodesStatements();
        createNodeStatements = cypherStatements(statements);
        assertEquals(1, createNodeStatements.size());
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`Topic`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(1, rows.size());
        }

        statements = compiler.createRelationshipsStatements();
        assertEquals(0, statements.size());
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

        mappingContext.registerNodeEntity(forum, forumId);
        mappingContext.registerNodeEntity(topic, topicId);
        mappingContext.registerRelationshipEntity(link, relationshipId);
        MappedRelationship mappedRelationship = new MappedRelationship(forumId, "HAS_TOPIC", topicId, relationshipId, Forum.class, ForumTopicLink.class);
        mappingContext.registerRelationship(mappedRelationship);

        // change the timestamp
        link.setTimestamp(327790L);

        // expect the property on the relationship entity to be updated on the graph relationship
        Compiler compiler = mapper.map(link).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.createNodesStatements();
        assertEquals(0, statements.size());
        statements = compiler.updateRelationshipStatements();
        assertEquals(1, statements.size());
        assertEquals("START r=rel({relIds}) FOREACH (row in filter(row in {rows} where row.relId = id(r)) | SET r += row.props) RETURN ID(r) as ref, ID(r) as id, {type} as type", statements.get(0).getStatement());
        List rows = (List) statements.get(0).getParameters().get("rows");
        assertEquals(1, rows.size());
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

        mappingContext.registerNodeEntity(forum, forumId);
        mappingContext.registerNodeEntity(topic, topicId);
        mappingContext.registerRelationshipEntity(link, linkId);

        // the mapping context remembers the relationship between the forum and the topic in the graph
        mappingContext.registerRelationship(new MappedRelationship(forumId, "HAS_TOPIC", topicId, Forum.class, ForumTopicLink.class));

        // unlink the objects manually
        forum.setTopicsInForum(null);
        link.setTopic(null);

        // expect the delete to be recognised when the forum is saved
        Compiler compiler = mapper.map(forum).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());
        List<Statement> statements = compiler.createRelationshipsStatements();
        assertEquals(0, statements.size());

        statements = compiler.deleteRelationshipStatements();
        assertEquals(1, statements.size());
        assertEquals("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`HAS_TOPIC`]->(endNode) DELETE rel",
                statements.get(0).getStatement());
        assertEquals(1, ((List)statements.get(0).getParameters().get("rows")).size());


        // expect the delete to be recognised if the RE is saved
//        expectOnSave(link, "MATCH ($0)-[_0:HAS_TOPIC]->($1) WHERE id($0)=0 AND id($1)=1 DELETE _0");
//
//        // expect nothing to happen if the topic is saved, because the domain model does not
//        // permit navigation from the topic to the RE (topic has no reference to it)
//        expectOnSave(topic, "");

        // todo: more tests re saving deletes from REs marked as incoming relationships

    }

    /**
     * @see DATAGRAPH-589
     */
    @Test
    public void createSimpleRelationshipWithIllegalCharactersBetweenObjects() {

        Artist theBeatles = new Artist("The Beatles");
        Album please = new Album("Please Please Me");
        theBeatles.getAlbums().add(please);
        please.setArtist(theBeatles);

        Compiler compiler = mapper.map(theBeatles).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.createNodesStatements();
        List<String> createNodeStatements = cypherStatements(statements);
        assertEquals(2, createNodeStatements.size());
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`l'artiste`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`l'album`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(1, rows.size());
        }

        statements = compiler.createRelationshipsStatements();
        List<String> createRelStatements = cypherStatements(statements);
        assertEquals(1, createRelStatements.size());
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`HAS-ALBUM`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(1, rows.size());
        }

    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void createOutgoingRelationWhenUnmarkedRelationIsSpecified() {

        Individual adam = new Individual();
        adam.setName("Adam");

        Individual vince = new Individual();
        vince.setName("Vince");

        adam.setFriends(Collections.singletonList(vince));

        Compiler compiler = mapper.map(adam).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.createNodesStatements();
        List<String> createNodeStatements = cypherStatements(statements);
        assertEquals(1, createNodeStatements.size());
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`Individual`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(2, rows.size());
        }

        statements = compiler.createRelationshipsStatements();
        List<String> createRelStatements = cypherStatements(statements);
        assertEquals(1, createRelStatements.size());
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`FRIENDS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(1, rows.size());
            assertEquals((long)-System.identityHashCode(adam), ((Map)rows.get(0)).get("startNodeId"));
            assertEquals((long)-System.identityHashCode(vince), ((Map)rows.get(0)).get("endNodeId"));
        }
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void createIncomingRelationWhenSpecified() {
        Mortal adam = new Mortal("Adam");
        Mortal vince = new Mortal("Vince");

        adam.getKnownBy().add(vince);

        Compiler compiler = mapper.map(adam).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.createNodesStatements();
        List<String> createNodeStatements = cypherStatements(statements);
        assertEquals(1, createNodeStatements.size());
        assertTrue(createNodeStatements.contains("UNWIND {rows} as row CREATE (n:`Mortal`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(2, rows.size());
        }

        statements = compiler.createRelationshipsStatements();
        List<String> createRelStatements = cypherStatements(statements);
        assertEquals(1, createRelStatements.size());
        assertTrue(createRelStatements.contains("UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`KNOWN_BY`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"));
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertEquals(1, rows.size());
            assertEquals((long)-System.identityHashCode(vince), ((Map)rows.get(0)).get("startNodeId"));
            assertEquals((long)-System.identityHashCode(adam), ((Map)rows.get(0)).get("endNodeId"));
        }
    }

    private Compiler mapAndCompile(Object object) {
        CompileContext context = this.mapper.map(object);
        Compiler compiler =  context.getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());
        return compiler;
    }

    private List<String> cypherStatements(List<Statement> statements) {
        List<String> cypher = new ArrayList<>(statements.size());
        for(Statement statement : statements) {
            cypher.add(statement.getStatement());
        }
        return cypher;
    }
}
