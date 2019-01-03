/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tells OGM to map values of a Map field in a node or relationship entity to properties of a node or a relationship
 * in the graph.
 * The property names are derived from field name or {@link #prefix()}, delimiter and keys in the Map. If the delimiter,
 * prefix or keys conflict with other field names in the class the behaviour is not defined.
 * Supported types for keys in the Map are String and Enum.
 * The values in the Map can be of any Java type equivalent to Cypher types. If full type information is provided other
 * Java types are also supported.
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
