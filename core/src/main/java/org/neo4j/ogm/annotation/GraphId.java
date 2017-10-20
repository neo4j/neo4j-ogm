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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies the field in the domain entity which is to be
 * mapped to the id property of its backing node in the graph.
 * This annotation is not needed if the domain entity has a Long
 * field called id.
 *
 * @author Vince Bickers
 * @deprecated Just replace {@link GraphId} with {@link Id} and {@link GeneratedValue}.
 * By default the {@link org.neo4j.ogm.id.InternalIdStrategy} will be used.
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface GraphId {

}

