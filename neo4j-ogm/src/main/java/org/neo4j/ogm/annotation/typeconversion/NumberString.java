package org.neo4j.ogm.annotation.typeconversion;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface NumberString {

    static final String CLASS = "org.neo4j.ogm.annotation.typeconversion.NumberString";
    static final String TYPE = "value";

    Class<? extends Number> value();
}

