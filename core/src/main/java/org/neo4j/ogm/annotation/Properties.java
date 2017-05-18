/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

/**
 * Tells OGM to map values of a Map field in a node or relationship entity to properties of a node or a relationship
 * in the graph.
 * <p>
 * The property names are derived from field name or {@link #prefix()}, delimiter and keys in the Map. If the delimiter,
 * prefix or keys conflict with other field names in the class the behaviour is not defined.
 * <p>
 * Supported types for keys in the Map are String and Enum.
 * <p>
 * The values in the Map can be of any Java type equivalent to Cypher types. If full type information is provided other
 * Java types are also supported.
 * <p>
 * If {@link #allowCast()} is set to true then types that can be cast to corresponding Cypher types are allowed as well.
 * Note that the original type cannot be deduced and the value will be deserialized to corresponding type - e.g.
 * when Integer instance is put to {@code Map<String, Object>} it will be deserialized as Long.
 *
 * @author Frantisek Hartman
 * @since 3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Properties {

    /**
     * Prefix for mapped properties, if not set the field name is used
     */
    String prefix() default "";

    /**
     * Delimiter to use in the property names
     */
    String delimiter() default ".";


    /**
     * If values in the Map that do not have supported Cypher type should be allowed to be cast to Cypher types
     */
    boolean allowCast() default false;
}
