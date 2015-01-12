package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface RelationshipEntity {

    static final String CLASS = "org.neo4j.ogm.annotation.RelationshipEntity";
    static final String TYPE = "type";

    String type() default "";
}
