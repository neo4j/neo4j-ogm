/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
import java.util.function.BiFunction;

/**
 * Tells OGM to map values of a Map field in a node or relationship entity to properties of a node or a relationship
 * in the graph.
 * The property names are derived from field name or {@link #prefix()}, delimiter and keys in the Map. If the delimiter,
 * prefix or keys conflict with other field names in the class the behaviour is not defined.
 * Supported types for keys in the Map are String and Enum.
 * The values in the Map can be of any Java type equivalent to Cypher types. Type derivation works best when you use concrete
 * types in the declaration of the map (i.e. {@code Map<String, Long>} as we are able to use this information most of the
 * time. In case you want Enum values, you must declare the composite property as {@code Map<SUPPORTED_KEY_TYPE, YourEnumType>}.
 * Otherwise only writing, but not reading into an enum is supported.
 * If {@link #allowCast()} is set to true then types that can be cast to corresponding Cypher types are allowed as well.
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 * @since 3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Properties {

    /**
     * Allows for specifying a prefix for the map properties. OGM defaults to the field name of the map attribute when not set.
     *
     * @return The prefix for mapped properties.
     */
    String prefix() default "";

    /**
     * @return Delimiter to use in the property names
     */
    String delimiter() default ".";

    /**
     * Some Java types, like Integer and Float, can be automatically cast into a wider type, like Long and Double, by
     * the Neo4j type systems. This in most cases not what one does expect during mapping. We have however not a way
     * to determine whether the type of the value put into the map was supposed originally when reading the instance
     * containing the map back. Set this attribute to true to allow OGM to accept the automatic cast.
     *
     * @return True, when the values of map entries are allowed to be cast to a wider datatype.
     */
    boolean allowCast() default false;

    /**
     * This attribute allows for configuring a transformation that is applied to enum properties. {@link Phase#TO_GRAPH} is applied
     * before the name of the enum is written to the graph, {@link Phase#TO_ENTITY} is applied before an instance of the enum
     * value is referenced.
     *
     * @return A transformation to be used on enum keys.
     */
    Class<? extends BiFunction<Phase, String, String>> transformEnumKeysWith() default NoopTransformation.class;

    /**
     * Phase of the mapping currently taking place.
     */
    enum Phase {
        /**
         * Properties are mapped to graph properties.
         */
        TO_GRAPH,
        /**
         * Graph properties are mapped to key/values of a map contained in an entity.
         */
        TO_ENTITY
    }

    class NoopTransformation implements BiFunction<Phase, String, String> {

        @Override
        public String apply(Phase phase, String s) {
            return s;
        }
    }
}
