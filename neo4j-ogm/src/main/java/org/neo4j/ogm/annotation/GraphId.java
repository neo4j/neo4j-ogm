package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

/**
 * Identifies the field in the domain entity which is to be
 * mapped to the id property of its backing node in the graph.
 *
 * This annotation is not needed if the domain entity has a Long
 * field called id.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface GraphId {

    static final String CLASS = "org.neo4j.ogm.annotation.GraphId";
    static final String NAME = "name";

    String name() default "";

}

