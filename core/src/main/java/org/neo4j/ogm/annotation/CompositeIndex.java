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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate a class to notify the OGM to create a composite index or node key constraint on
 * given properties for a label of annotated class.
 * <p>
 * By default a composite index is created, setting {@link #unique()} to true will create a node key constraint.
 * <p>
 * Supported only on enterprise edition of Neo4j.
 *
 * @author Frantisek Hartman
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Repeatable(CompositeIndexes.class)
@Inherited
public @interface CompositeIndex {

    /**
     * Alias for {@link #properties()}
     */
    String[] value() default {};

    /**
     * Names of the properties on which a composite index should be created.
     * <p>
     * All property names must match an existing property in the class or one of its super classes.
     * If a property name is overridden by @Property annotation this name must be used.
     * <p>
     * Order of the properties matters, check Neo4j documentation for composite indexes for details.
     */
    String[] properties() default {};

    /**
     * Indicates whether to apply a node key constraints on the properties.
     * Defaults to false.
     */
    boolean unique() default false;
}
