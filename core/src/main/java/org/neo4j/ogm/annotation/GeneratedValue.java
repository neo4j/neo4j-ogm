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
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.neo4j.ogm.id.IdStrategy;
import org.neo4j.ogm.id.InternalIdStrategy;

/**
 * Used to generate an ID. Must be used with the @{@link Id} annotation, otherwise it will be ignored.
 * <p>
 * Two strategies are provided {@link org.neo4j.ogm.id.UuidStrategy} and {@link org.neo4j.ogm.id.InternalIdStrategy}.
 * <p>
 * Custom strategies may be implemented using {@link IdStrategy}
 *
 * @since 3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface GeneratedValue {

	/**
	 * (Optional) The primary key generation strategy
	 * that the persistence provider must use to
	 * generate the annotated entity id.
	 */
	Class<? extends IdStrategy> strategy() default InternalIdStrategy.class;
}
