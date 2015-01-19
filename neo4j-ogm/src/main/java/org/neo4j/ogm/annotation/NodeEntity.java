package org.neo4j.ogm.annotation;

import java.lang.annotation.*;


/**
 * Identifies a domain entity as being backed by a node in the graph.
 *
 * This annotation is not needed if the domain entity's simple classname
 * matches at least one of the labels of the node in the graph (case
 * insensitive)
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface NodeEntity {

    static final String CLASS = "org.neo4j.ogm.annotation.NodeEntity";
    static final String LABEL = "label";

    String label() default "";
}
