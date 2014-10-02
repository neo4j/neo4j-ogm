package org.neo4j.ogm.mapper.model.education;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.junit.Test;
import org.neo4j.ogm.mapper.GraphModelToObjectMapper;
import org.neo4j.ogm.mapper.domain.education.Course;
import org.neo4j.ogm.mapper.domain.education.Student;
import org.neo4j.ogm.mapper.domain.education.Teacher;
import org.neo4j.ogm.mapper.model.DummyRequest;
import org.neo4j.ogm.strategy.simple.SimpleSetterMappingStrategy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class EducationTest {

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
    public void testFetchCoursesTaughtByTeacher() throws Exception {

        Map<String, Teacher> teachers = loadTeachers();

        for (Teacher teacher : teachers.values()) {
            loadCourses(teacher);
        }

        Teacher mrThomas = teachers.get("Mr Thomas");
        Teacher mrsRoberts = teachers.get("Mrs Roberts");
        Teacher missYoung = teachers.get("Miss Young");

        checkCourseNames(mrThomas, "Maths", "English", "Physics");
        checkCourseNames(mrsRoberts, "English", "History", "PE");
        checkCourseNames(missYoung, "History", "Geography", "Philosophy and Ethics");

        for (Teacher teacher : teachers.values()) {
            for (Course course : teacher.getCourses()) {
                List<Student> students = course.getStudents();
                if (course.getName().equals("Maths")) checkMaths(students);
                else if (course.getName().equals("Physics")) checkPhysics(students);
                else if (course.getName().equals("Philosophy and Ethics")) checkPhilosophyAndEthics(students);
                else if (course.getName().equals("PE")) checkPE(students);
                else if (course.getName().equals("History")) checkHistory(students);
                else if (course.getName().equals("Geography")) checkGeography(students);
                else checkEnglish(students);

            }
        }
    }

    private void test(long hash, List<Student> students) {
        for (Student student : students) {
            hash-= student.getId();
        }
        assertEquals(0, hash);
    }

    // all students study english
    private void checkEnglish(List<Student> students) {
        long hash = 0;
        for (int i = 101; i < 127; i++) {
            hash+=i;
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
        for (int i = 102; i < 127; i+=2) {
            hash+=i;
        }
        test(hash, students);
    }

    // every 3rd student studies PE
    private void checkPE(List<Student> students) {
        long hash = 0;
        for (int i = 103; i < 127; i+=3) {
            hash+=i;
        }
        test(hash, students);
    }

    // all students are deep thinkers
    private void checkPhilosophyAndEthics(List<Student> students) {
        long hash = 0;
        for (int i = 101; i < 127; i++) {
            hash+=i;
        }
        test(hash, students);
    }

    // all students with odd ids study physics
    private void checkPhysics(List<Student> students) {
        long hash = 0;
        for (int i = 101; i < 127; i+=2) {
            hash+=i;
        }
        test(hash, students);
    }

    // all students study maths
    private void checkMaths(List<Student> students) {
        long hash = 0;
        for (int i = 101; i < 127; i++) {
            hash+=i;
        }
        test(hash, students);
    }

    private Map<String, Teacher> loadTeachers() throws Exception {

        SimpleSetterMappingStrategy mapper = new SimpleSetterMappingStrategy(Teacher.class);
        // indicates we're starting a brand new domain parse. Throw everything away that's in the object cache
        // Note: normally wouldn't be required, but the test classes create objects with non-unique ids,
        // so the tests interfere with each other :).
        mapper.reset();

        GraphModel graphModel;

        Map<String, Teacher> teachers = new HashMap<>();
        DummyRequest request = new TeacherRequest();

        while ((graphModel = request.getResponse().next()) != null) {
            Teacher teacher = (Teacher) mapper.mapToObject(graphModel);
            teachers.put(teacher.getName(), teacher);
        }

        return teachers;
    }

    // when we hydrate a set of things that are previously loaded we don't need to create them afresh
    // - the object map of the existing objects is simply extended with new data.
    private void loadCourses(Teacher teacher) throws Exception {

        GraphModel graphModel;
        DummyRequest request = selectRequest(teacher);

        GraphModelToObjectMapper mapper = new SimpleSetterMappingStrategy(Course.class);
        while ((graphModel = request.getResponse().next()) != null) {
            mapper.mapToObject(graphModel);
        }
    }

    private DummyRequest selectRequest(Teacher teacher) {
        if (teacher.getName().equals("Mr Thomas")) return new CourseRequestMrThomas();
        if (teacher.getName().equals("Mrs Roberts")) return new CourseRequestMrsRoberts();
        return new CourseRequestMissYoung();
    }

    private void checkCourseNames(Teacher teacher, String... courseNames) {

        int n = courseNames.length;
        List<String> test = Arrays.asList(courseNames);

        for (Course course : teacher.getCourses()) {
            if (test.contains(course.getName())) {
                n--;
            }
        }
        assertEquals(0, n);
    }
}
