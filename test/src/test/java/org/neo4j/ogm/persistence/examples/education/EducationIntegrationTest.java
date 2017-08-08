/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.persistence.examples.education;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.education.Course;
import org.neo4j.ogm.domain.education.School;
import org.neo4j.ogm.domain.education.Student;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Luanne Misquitta
 */
public class EducationIntegrationTest extends MultiDriverTestClass {

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() throws IOException {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.education");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
    }

    @Test
    public void loadingCourseByPropertyShouldNotLoadOtherEntitiesWithSamePropertyValue() {
        //create a course
        Course course = new Course("CompSci");
        //create a student with the same name as the course
        Student student = new Student("CompSci");
        //relate them so they're both in the mappingContext
        course.setStudents(Collections.singletonList(student));
        session.save(course);

        //fetch Courses by name
        Collection<Course> courses = session.loadAll(Course.class, new Filter("name", ComparisonOperator.EQUALS, "CompSci"));
        assertThat(courses).hasSize(1);
        assertThat(courses.iterator().next()).isEqualTo(course);
        assertThat(courses.iterator().next().getStudents()).hasSize(1);
    }

    /**
     * @see DATAGRAPH-595
     */
    @Test
    public void loadingASchoolWithNegativeDepthShouldLoadAllConnectedEntities() {
        //Create students, teachers, courses and a school
        School hogwarts = new School("Hogwarts");

        Student harry = new Student("Harry Potter");
        Student ron = new Student("Ron Weasley");
        Student hermione = new Student("Hermione Granger");

        Course transfiguration = new Course("Transfiguration");
        transfiguration.setStudents(Arrays.asList(harry, hermione, ron));

        Course potions = new Course("Potions");
        potions.setStudents(Arrays.asList(ron, hermione));

        Course dark = new Course("Defence Against The Dark Arts");
        dark.setStudents(Collections.singletonList(harry));

        Teacher minerva = new Teacher("Minerva McGonagall");
        minerva.setCourses(Collections.singletonList(transfiguration));
        minerva.setSchool(hogwarts);

        Teacher severus = new Teacher("Severus Snape");
        severus.setCourses(Arrays.asList(potions, dark));
        severus.setSchool(hogwarts);

        hogwarts.setTeachers(Arrays.asList(minerva, severus));
        session.save(hogwarts);

        session.clear();
        //Load the school with depth -1
        hogwarts = session.load(School.class, hogwarts.getId(), -1);
        assertThat(hogwarts.getTeachers()).hasSize(2);
        for (Teacher teacher : hogwarts.getTeachers()) {
            if (teacher.getName().equals("Severus Snape")) {
                assertThat(teacher.getCourses()).hasSize(2);
                for (Course course : teacher.getCourses()) {
                    if (course.getName().equals("Potions")) {
                        assertThat(course.getStudents()).hasSize(2);
                    } else {
                        assertThat(course.getStudents()).hasSize(1);
                    }
                }
            } else {
                assertThat(teacher.getCourses()).hasSize(1);
                assertThat(teacher.getCourses().get(0).getStudents()).hasSize(3);
            }
        }
    }
}
