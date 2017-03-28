package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

/**
 * Informs the OGM that the method annotated with this should be run one the object is loaded from the database.
 *
 * @since 3.0
 * @author Mark Angrish
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface PostLoad {

}
