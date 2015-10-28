/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.mapper;

import java.util.Collection;

/**
 * Interface to map a row-based response into one or more objects
 *
 * @param <T> The type of object onto which the response should be mapped
 */
public interface RowMapper<T> {

    /**
     * Appends elements to the given result for each of the row values.
     *
     * @param result The collection into which mapped objects are to be added
     * @param rowValues The value in the row returned from the graph database
     * @param responseColumns The names of the columns in the row
     */
    void map(Collection<T> result, Object[] rowValues, String[] responseColumns);

}
