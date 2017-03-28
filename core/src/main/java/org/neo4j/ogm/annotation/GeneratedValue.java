package org.neo4j.ogm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to generate an ID. Must be used with the <code>@Id</code> annotation.
 *
 * Currently the two supported strategies are UUID and Internal Neo4j Id.
 *
 * @since 3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface GeneratedValue {

	/**
	 * (Optional) The primary key generation strategy
	 * that the persistence provider must use to
	 * generate the annotated entity id.
	 */
	GenerationType strategy() default GenerationType.NEO4J_INTERNAL_ID;
}
