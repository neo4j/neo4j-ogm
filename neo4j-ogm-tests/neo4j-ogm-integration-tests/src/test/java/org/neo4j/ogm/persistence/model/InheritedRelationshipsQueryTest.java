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

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.gh670.Course;
import org.neo4j.ogm.domain.gh670.Klassenclown;
import org.neo4j.ogm.domain.gh670.Person;
import org.neo4j.ogm.domain.gh670.Teacher;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * This tests whether a query for a common base class, returning concrete instances matched by the most specific labels
 * actually returns instances that have their relationships filled according to the specified depth.
 *
 * @author Michael J. Simons
 * @soundtrack Paul van Dyk - From Then On
 */
public class InheritedRelationshipsQueryTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private static Long idOfCreatedTeacher;

    @BeforeClass
    public static void setUpTestDatabase() {

        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.gh670");
        Session session = sessionFactory.openSession();
        Teacher teacher = new Teacher();
        teacher.setName("Edna Krabappel");
        Course course = new Course();
        course.setTitle("Math");

        Klassenclown bart = new Klassenclown();
        bart.setName("Bart Simpson");
        course.setTakenBy(Collections.singletonList(bart));

        teacher.getCources().add(course);
        session.save(teacher);

        Iterable<Map<String, Object>> results = session
            .query("MATCH (t:Teacher) WHERE t.name = $name RETURN id(t) as id",
                Collections.singletonMap("name", teacher.getName())).queryResults();
        assertThat(results).hasSize(1).allSatisfy(row -> assertThat(row).containsKeys("id"));

        idOfCreatedTeacher = (Long) results.iterator().next().get("id");
    }

    @Test // GH-670
    public void shouldReturnConcreteClassWithRelationships() {

        Session session = sessionFactory.openSession();

        Collection<Person> persons = session.loadAll(Person.class, 1);
        assertThat(persons).hasSize(2).allSatisfy(p -> {

            List<Course> courses;
            if (p instanceof Teacher) {
                Teacher t = (Teacher) p;
                courses = t.getCources();
            } else if (p instanceof Klassenclown) {
                Klassenclown c = (Klassenclown) p;
                courses = c.getCoursesTaken();
            } else {
                courses = Collections.emptyList();
            }
            assertThat(courses).hasSize(1).extracting(Course::getTitle).contains("Math");
        });
    }
}
