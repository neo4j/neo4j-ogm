package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface CustomType {

    static final String CLASS = "org.neo4j.ogm.annotation.CustomType";
    static final String CONVERTER = "value";

    Class value();

}

