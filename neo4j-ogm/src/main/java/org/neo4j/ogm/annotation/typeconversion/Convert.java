package org.neo4j.ogm.annotation.typeconversion;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface Convert {

    static final String CLASS = "org.neo4j.ogm.annotation.typeconversion.Convert";
    static final String CONVERTER = "value";

    Class value();

}

