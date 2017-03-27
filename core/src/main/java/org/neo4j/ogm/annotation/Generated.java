package org.neo4j.ogm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to generate an ID. Must be used with the <code>@Id</code> annotation.
 *
 * Currently the two supported strategies are UUID and Internal Neo4j Id.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Generated {

}
