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
import java.util.HashMap;
import java.util.Map;

/**
 * A {@code Map}-based implementation of {@link RowMapper}.
 */
public class MapRowModelMapper implements RowMapper<Map<String, Object>> {

    @Override
    public void map(Collection<Map<String, Object>> result, Object[] rowValues, String[] responseVariables) {

        Map<String, Object> element = new HashMap<>();
        for (int i = 0; i < responseVariables.length; i++) {
            element.put(responseVariables[i], rowValues[i]);
        }
        result.add(element);
    }

}
