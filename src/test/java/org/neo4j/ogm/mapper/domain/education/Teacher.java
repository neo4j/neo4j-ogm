package org.neo4j.ogm.mapper.domain.education;

import java.util.List;

public class Teacher  {

    // injected at compile time
    //DummyRequest request;

    String name;

    List<Course> courses;

    Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // @Lazy
    public List<Course> getCourses() {

        /**
         * what we might do with an annotation processor : this HAS to be thread safe.
         */
//        if (courses == null) { // or some other means of detecting if we're hydrated.
//
//            // DummyRequest = mapper.query("MATCH (c:COURSE)--(o) return o
//
//
//            try {
//                GraphModelToObjectMapper mapper = new SimpleSetterMappingStrategy(Course.class);
//                GraphModel graphModel;
//                while ((graphModel = request.getResponse().next()) != null) {
//                    mapper.mapToObject(graphModel);
//                }
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
        // end of injected code

        return courses;
    }

    public void setCourses(List<Course> courses) {
        // persistable?
        this.courses = courses;
    }


}
