package org.neo4j.ogm.annotation.typeconversion;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface DateLong {
    static final String CLASS = "org.neo4j.ogm.annotation.typeconversion.DateLong";
}

