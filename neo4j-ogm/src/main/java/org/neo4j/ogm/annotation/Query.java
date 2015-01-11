package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@Inherited
public @interface Query {

    static final String CLASS = "org.neo4j.ogm.annotation.Query";
    static final String VALUE = "value";

    String value() default "";
}
