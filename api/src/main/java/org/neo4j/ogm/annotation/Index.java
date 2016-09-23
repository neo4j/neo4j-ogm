/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */
package org.neo4j.ogm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields and properties marked with this annotation will notify the OGM that they
 * should be used as part of an index.
 * Index generation behaviour can be defined in <code>ogm.properties</code> by
 * defining a property called: <code>indexes.auto</code> and providing
 * a value of:
 * <ul>
 * <li><code>create-drop</code>: create constraints and indexes on startup and
 * then destroy them on shutdown. This is an excellent option for developers.</li>
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
@Target(value = {ElementType.FIELD, ElementType.TYPE,
		ElementType.METHOD})
public @interface Index {

	/**
	 * Indicates whether to apply a unique constraint on this property, defaults to
	 * false.
	 */
	boolean unique() default false;
}
