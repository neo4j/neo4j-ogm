package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface Property {

    static final String CLASS = "org.neo4j.ogm.annotation.Property";
    static final String NAME = "name";

    String name() default "";
}
