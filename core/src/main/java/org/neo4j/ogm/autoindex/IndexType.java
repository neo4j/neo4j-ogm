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

package org.neo4j.ogm.autoindex;

/**
 * @author Frantisek Hartman
 */
enum IndexType {

    /**
     * Single property index
     */
    SINGLE_INDEX,

    /**
     * Composite index
     */
    COMPOSITE_INDEX,

    /**
     * Unique constraint
     */
    UNIQUE_CONSTRAINT,

    /**
     *
     */
    NODE_KEY_CONSTRAINT,

    /**
     * Node property existence constraint
     */
    NODE_PROP_EXISTENCE_CONSTRAINT,

    /**
     * Relationship property existence constraint
     */
    REL_PROP_EXISTENCE_CONSTRAINT,

}
