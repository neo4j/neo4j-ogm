/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices and license terms. Your use of the source code for these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 *
 */

package org.neo4j.ogm.result.adapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.response.model.DefaultRowModel;

/**
 * This adapter will transform a Map&lt;String,Object&gt; typically representing an Embedded or Bolt response into a {@link RowModel} response
 *
 * @author vince
 * @author Luanne Misquitta
 */
public abstract class RowModelAdapter implements ResultAdapter<Map<String, Object>, RowModel> {


    private List<String> columns = new ArrayList<>();

    /**
     * Reads the next row from the result object and transforms it into a RowModel object
     *
     * @param data the data to transform, given as a map
     * @return @return the data transformed to an {@link RowModel}
     */
    public RowModel adapt(Map<String, Object> data) {

        if (columns == null) {
            throw new ResultProcessingException("Result columns should not be null");
        }


        // there is no guarantee that the objects in the data are ordered the same way as required by the columns
        // so we use the columns information to extract them in the correct order for post-processing.
        Iterator<String> iterator = columns.iterator();

        List<String> variables = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        while (iterator.hasNext()) {

            String key = iterator.next();
            Object value = data.get(key);

            if (isPath(value) || isNode(value) || isRelationship(value)) {
                continue;
            }
            variables.add(key);

            if (value!=null && value.getClass().isArray()) {
                values.add(AdapterUtils.convertToIterable(value));
            }

            values.add(value);
        }

        return new DefaultRowModel(values.toArray(new Object[] {}), variables.toArray(new String[] {}));
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public abstract boolean isPath(Object value);

    public abstract boolean isNode(Object value);

    public abstract boolean isRelationship(Object value);

}
