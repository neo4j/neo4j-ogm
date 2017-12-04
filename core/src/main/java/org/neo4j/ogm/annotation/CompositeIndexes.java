package org.neo4j.ogm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Frantisek Hartman
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Inherited
public @interface CompositeIndexes {

    CompositeIndex[] value();
}
