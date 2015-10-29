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

/**
 * {@link RowMapper} that maps each row value onto
 *
 * @param <T> The type of entity to which the row is to be mapped
 */
public class EntityRowModelMapper<T> implements RowMapper<T>, Mapper<Response<RowModel>> {

    @Override
    @Deprecated
    @SuppressWarnings("unchecked")
    public void map(Collection<T> result, Object[] rowValues, String[] responseVariables) {
        if (responseVariables.length > 1) {
            throw new RuntimeException(
                    "Scalar response queries must only return one column. Make sure your cypher query only returns one item.");
        }

        for (int i = 0; i < responseVariables.length; i++) {
            result.add((T) rowValues[i]);
        }
    }

    @Override
    public <T> Iterable<T> map(Class<T> type, Response<RowModel> response) {

        Collection<T> result = new ArrayList<>();

        RowModel model;
        while ((model = response.next()) != null) {

            if (model.variables().length > 1) {
                throw new RuntimeException(
                        "Scalar response queries must only return one column. Make sure your cypher query only returns one item.");
            }
            for (int i = 0; i < model.variables().length; i++) {
                result.add((T) model.getValues()[0]);
            }
        }

        return result;
    }
}
