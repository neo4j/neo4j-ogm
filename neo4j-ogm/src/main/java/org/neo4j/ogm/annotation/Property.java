package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

/**
 * Establishes the mapping between a domain entity attribute
 * and a node or relationship property in the graph.
 *
 * This annotation is not needed if the mapping can be
 * derived by the OGM, according to the following
 * heuristics:
 *
 *      an accessor method
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface Property {

    static final String CLASS = "org.neo4j.ogm.annotation.Property";
    static final String NAME = "name";

    String name() default "";
}
