package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

/**
 * Identifies a domain entity as being backed by a relationship in the graph.
 *
 * This annotation is always needed for relationship-backed entities.
 *
 * The type attribute supplies the relatoionship-type in the graph, and
 * can be omitted if the domain entity's simple class name matches
 * exactly the relationship type.
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface RelationshipEntity {

    static final String CLASS = "org.neo4j.ogm.annotation.RelationshipEntity";
    static final String TYPE = "type";

    String type() default "";
}
