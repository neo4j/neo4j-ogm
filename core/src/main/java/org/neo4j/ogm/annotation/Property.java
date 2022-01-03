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

/**
 * Establishes the mapping between a domain entity attribute
 * and a node or relationship property in the graph.
 * This annotation is not needed if the mapping can be
 * derived by the OGM, according to the following
 * heuristics:
 * an accessor method
 *
 * @author Vince Bickers
 * @author Gerrit Meier
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Property {

    String NAME = "name";

    /**
     * Name of the property in the graph
     */
    String name() default "";

    @ValueFor(NAME)
    String value() default "";

    /**
     * Set this attribute to {@literal true} to prevent writing any value of this property to the graph.
     * @return
     */
    boolean readOnly() default false;
}
