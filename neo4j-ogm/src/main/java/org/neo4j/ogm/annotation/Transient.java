package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

/**
 * By default all domain entity types will be persisted unless they are
 * annotated with @Transient, or are non-annotated abstract classes.
 *
 * This annotation can be placed on types, fields and methods
 * and the OGM will ignore any object or object reference with
 * the annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface Transient {
    static final String CLASS = "org.neo4j.ogm.annotation.Transient";
}

