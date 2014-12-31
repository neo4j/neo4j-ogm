package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface EnumString {

    static final String CLASS = "org.neo4j.ogm.annotation.EnumString";
    static final String ENUM = "value";

    Class<? extends Enum> value();
}

