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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields and properties marked with this annotation will notify the OGM that they
 * should be used as part of an index and/or for use during lookups and merging.
 * Fields marked with <code>unique=false</code> and <code>primary=true</code> will be ignored.
 * Only one index per class hierarchy may be marked as <code>primary</code>.
 * If index auto generation is turned on then classes containing <code>@Index</code>
 * will be used. Indexes will always be generated with the containing class's label and
 * the annotated property's name.
 * Index generation behaviour can be defined in <code>ogm.properties</code> by
 * defining a property called: <code>indexes.auto</code> and providing
 * a value of:
 * <ul>
 * <li><code>assert</code>: drop all indexes and constraints then create
 * constraints and indexes on startup. No indexes or constraints will be dropped on
 * shutdown.</li>
 * <li><code>validate</code>: confirm that the required indexes and constraints
 * defined already exist on startup otherwise abort startup</li>
 * <li><code>dump</code>: will generate a file in the current directory with the
 * cypher commands to create indexes and constraints. Before doing this it will run the
 * same behaviour as validate.</li>
 * <li><code>none</code>: do not generate any constraints or indexes
 * <strong>[default]</strong></li>
 * </ul>
 *
 * @author Mark Angrish
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Index {

    /**
     * Indicates whether to apply a unique constraint on this property, defaults to
     * false.
     */
    boolean unique() default false;
}
