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
package org.neo4j.ogm.persistence.examples.education;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.neo4j.ogm.domain.education.Course;
import org.neo4j.ogm.domain.education.Student;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.session.Neo4jSession;

/**
 * @author Vince Bickers
 */
public class EducationTest {

    private static MetaData metadata = new MetaData("org.neo4j.ogm.domain.education");
    private static Neo4jSession session = new Neo4jSession(metadata, true, new EducationRequest());

    @Test
    public void testTeachers() throws Exception {

        Map<String, Teacher> teachers = loadTeachers();

        Teacher mrThomas = teachers.get("Mr Thomas");
        Teacher mrsRoberts = teachers.get("Mrs Roberts");
        Teacher missYoung = teachers.get("Miss Young");

        checkCourseNames(mrThomas, "Maths", "English", "Physics");
        checkCourseNames(mrsRoberts, "English", "History", "PE");
        checkCourseNames(missYoung, "History", "Geography", "Philosophy and Ethics");
    }

    @Test
    public void testFetchCoursesTaughtByAllTeachers() throws Exception {

        Map<String, Teacher> teachers = loadTeachers();  // note: idempotent!

        // this response is for an imagined request: "match p = (c:COURSE)--(o) where id(c) in [....] RETURN p"
        // i.e. we have a set of partially loaded courses attached to our teachers which we now want to
        // hydrate by getting all their relationships
        hydrateCourses(teachers.values());

        Set<Course> courses = new HashSet<>();
        for (Teacher teacher : teachers.values()) {
            for (Course course : teacher.getCourses()) {
                if (!courses.contains(course)) {
                    List<Student> students = course.getStudents();
                    switch (course.getName()) {
                        case "Maths":
                            checkMaths(students);
                            break;
                        case "Physics":
                            checkPhysics(students);
                            break;
                        case "Philosophy and Ethics":
                            checkPhilosophyAndEthics(students);
                            break;
                        case "PE":
                            checkPE(students);
                            break;
                        case "History":
                            checkHistory(students);
                            break;
                        case "Geography":
                            checkGeography(students);
                            break;
                        default:
                            checkEnglish(students);
                            break;
                    }
                    courses.add(course);
                }
            }
        }
        assertThat(courses).hasSize(7);
    }

    private void test(long hash, List<Student> students) {
        for (Student student : students) {
            hash -= student.getId();
        }
        assertThat(hash).isEqualTo(0);
    }

    // all students study english
    private void checkEnglish(List<Student> students) {
        long hash = 0;
        for (int i = 101; i < 127; i++) {
            hash += i;
        }
        test(hash, students);
    }

    // all students whose ids modulo 100 are prime study geography. 1 is not considered prime
    private void checkGeography(List<Student> students) {
        long hash = 102 + 103 + 105 + 107 + 111 + 113 + 117 + 119 + 123;

        test(hash, students);
    }

    // all students with even ids study history
    private void checkHistory(List<Student> students) {
        long hash = 0;
        for (int i = 102; i < 127; i += 2) {
            hash += i;
        }
        test(hash, students);
    }

    // every 3rd student studies PE
    private void checkPE(List<Student> students) {
        long hash = 0;
        for (int i = 103; i < 127; i += 3) {
            hash += i;
        }
        test(hash, students);
    }

    // all students are deep thinkers
    private void checkPhilosophyAndEthics(List<Student> students) {
        long hash = 0;
        for (int i = 101; i < 127; i++) {
            hash += i;
        }
        test(hash, students);
    }

    // all students with odd ids study physics
    private void checkPhysics(List<Student> students) {
        long hash = 0;
        for (int i = 101; i < 127; i += 2) {
            hash += i;
        }
        test(hash, students);
    }

    // all students study maths
    private void checkMaths(List<Student> students) {
        long hash = 0;
        for (int i = 101; i < 127; i++) {
            hash += i;
        }
        test(hash, students);
    }

    private Map<String, Teacher> loadTeachers() {

        Map<String, Teacher> teachers = new HashMap<>();
        Collection<Teacher> teacherList = session.loadAll(Teacher.class);

        for (Teacher teacher : teacherList) {
            teachers.put(teacher.getName(), teacher);
        }

        return teachers;
    }

    private void hydrateCourses(Collection<Teacher> teachers) {

        session.loadAll(Course.class);
    }

    private void checkCourseNames(Teacher teacher, String... courseNames) {

        int n = courseNames.length;
        List<String> test = Arrays.asList(courseNames);

        for (Course course : teacher.getCourses()) {
            if (test.contains(course.getName())) {
                n--;
            }
        }
        assertThat(n).isEqualTo(0);
    }
}
