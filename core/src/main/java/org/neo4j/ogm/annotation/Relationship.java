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

import java.lang.annotation.*;

/**
 * Identifies field that is to be represented as a relationship
 *
 * @author Vince Bickers
 * @author Frantisek Hartman
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Relationship {

    String TYPE = "type";
    String DIRECTION = "direction";

    String INCOMING = "INCOMING";
    String OUTGOING = "OUTGOING";
    String UNDIRECTED = "UNDIRECTED";

    /**
     * Type of the relationship, defaults to name of the field in SNAKE_CASE
     */
    String type() default "";

    /**
     * Direction of the relationship. Defaults to OUTGOING.
     *
     * Possible values are {@link #OUTGOING}, {@link #INCOMING}, {@link #UNDIRECTED}.
     */
    String direction() default OUTGOING;
}
