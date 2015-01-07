package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface Transient {
    static final String CLASS = "org.neo4j.ogm.annotation.Transient";
}

