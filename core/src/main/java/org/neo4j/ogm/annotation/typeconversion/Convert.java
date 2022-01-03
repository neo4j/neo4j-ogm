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
package org.neo4j.ogm.annotation.typeconversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * Annotation to be applied to fields of entity properties to specify the AttributeConverter to use for
 * writing or reading its value in the graph database.
 *
 * @author Vince Bickers
 * @author Adam George
 * @author Michael J. Simons
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Convert {

    String CONVERTER = "value";
    String GRAPH_TYPE = "graphPropertyType";

    /**
     * The type of {@link org.neo4j.ogm.typeconversion.AttributeConverter} implementation to use on this property.
     */
    Class<?> value() default Unset.class;

    /**
     * The type to which the value of the annotated member should be converted before saving as a property in the graph
     * database.
     * <p>
     * This is an optional attribute that should only be needed if {@link #value()} hasn't been set to an explicit
     * converter, as it only gets used to look up converters that can do the job at runtime.
     * </p>
     */
    Class<?> graphPropertyType() default Unset.class;

    /**
     * Placeholder to allow the annotation to be applied without specifying an explicit converter implementation.
     */
    abstract class Unset implements AttributeConverter<Object, Object> {

    }
}

