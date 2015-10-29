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

import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.response.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@code Map}-based implementation of {@link RowMapper}.
 */
public class MapRowModelMapper implements RowMapper<Map<String, Object>>, Mapper<Response<RowModel>> {

    @Override
    public void map(Collection<Map<String, Object>> result, Object[] rowValues, String[] responseVariables) {

        Map<String, Object> element = new HashMap<>();
        for (int i = 0; i < responseVariables.length; i++) {
            element.put(responseVariables[i], rowValues[i]);
        }
        result.add(element);
    }

    @Override
    public <T> Iterable<T> map(Class<T> type, Response<RowModel> response) {

        Collection<Map<String, Object>> result = new ArrayList<>();

        RowModel model;

        while ((model = response.next()) != null) {
            Map<String, Object> element = new HashMap<>();
            for (int i = 0; i < model.variables().length; i++) {
                element.put(model.variables()[i], model.getValues()[i]);
            }
            result.add(element);
        }

        return (Iterable<T>) result;
    }
}
