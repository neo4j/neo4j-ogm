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

/**
 * Represents the primary unique constraint used to reference an EntityNode.
 *
 * <p>When using an @Id on a class attribute, this attribute will be considered as the key of the entity,
 * and saving to the database will trigger a merge on an existing entry with the same key if it exists.
 * The @Id annotated attribute can either be assigned manually by the user (default), or can be generated
 * by OGM (see @{@link GeneratedValue}}.
 *
 * <p>This comes as a more explicit replacement to the old {@link Index}(primary = true, unique = true) annotation.
 *
 * @since 3.0
 * @author Mark Angrish
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Id {

}
