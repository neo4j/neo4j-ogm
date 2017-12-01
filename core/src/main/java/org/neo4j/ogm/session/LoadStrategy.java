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

package org.neo4j.ogm.session;

/**
 * @author Frantisek Hartman
 */
public enum LoadStrategy {

    /**
     * Load strategy which fetches related nodes by querying all paths from matched nodes, resulting into pattern
     * similar to
     * {@code MATCH p=(n)-[*0..n]-() RETURN p}
     */
    PATH_LOAD_STRATEGY,

    /**
     * Load strategy which uses nested list comprehensions to get related nodes based on the schema generated from
     * entity classes
     * NOTE: Does not support queries with unlimited depth
     */
    SCHEMA_LOAD_STRATEGY;
}
