package org.neo4j.ogm.annotation.typeconversion;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface EnumString {

    static final String CLASS = "org.neo4j.ogm.annotation.typeconversion.EnumString";
    static final String TYPE = "value";

    Class<? extends Enum> value();
}

