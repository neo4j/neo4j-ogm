package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface NodeEntity {

    static final String CLASS = "org.neo4j.ogm.annotation.NodeEntity";
    static final String LABEL = "label";

    String label() default "";
}
