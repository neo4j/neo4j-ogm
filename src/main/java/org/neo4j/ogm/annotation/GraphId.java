package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface GraphId {

    static final String CLASS = "org.neo4j.ogm.annotation.GraphId";
    static final String NAME = "name";

    String name() default "";

}

