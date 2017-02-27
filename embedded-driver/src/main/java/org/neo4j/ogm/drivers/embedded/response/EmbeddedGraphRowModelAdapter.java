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

package org.neo4j.ogm.drivers.embedded.response;

import java.util.*;

import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowModel;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.response.model.DefaultGraphModel;
import org.neo4j.ogm.response.model.DefaultGraphRowModel;
import org.neo4j.ogm.response.model.DefaultRowModel;
import org.neo4j.ogm.result.adapter.AdapterUtils;
import org.neo4j.ogm.result.adapter.GraphModelAdapter;
import org.neo4j.ogm.result.adapter.GraphRowModelAdapter;

/**
 * This adapter will transform an embedded response into a json response
 *
 * @author vince
 */
public class EmbeddedGraphRowModelAdapter extends GraphRowModelAdapter {

    private List<String> columns = new ArrayList<>();

    public EmbeddedGraphRowModelAdapter(GraphModelAdapter graphModelAdapter) {
        super(graphModelAdapter);
    }

    /**
     * Reads the next row from the result object and transforms it into a RowModel object
     *
     * @param data the data to transform, given as a map
     * @return @return the data transformed to an {@link RowModel}
     */
    public GraphRowModel adapt(Map<String, Object> data) {

        if (columns == null) {
            throw new ResultProcessingException("Column data cannot be null!");
        }

        Set<Long> nodeIdentities = new HashSet();
        Set<Long> edgeIdentities = new HashSet();

        GraphModel graphModel = new DefaultGraphModel();
        List<String> variables = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        // there is no guarantee that the objects in the data are ordered the same way as required by the columns
        // so we use the columns information to extract them in the correct order for post-processing.
        Iterator<String> iterator = columns.iterator();

        adapt(iterator, data, graphModel, variables, values, nodeIdentities, edgeIdentities);

        DefaultRowModel rowModel = new DefaultRowModel(values.toArray(new Object[]{}), variables.toArray(new String[]{}));

        return new DefaultGraphRowModel(graphModel, rowModel.getValues());
    }

    private void adapt(Iterator<String> iterator, Map<String, Object> data, GraphModel graphModel, List<String> variables, List<Object> values, Set<Long> nodeIdentities, Set<Long> edgeIdentities) {

        while (iterator.hasNext()) {

            String key = iterator.next();
            variables.add(key);

            Object value = data.get(key);

            if (value.getClass().isArray()) {
                value = AdapterUtils.convertToIterable(value);
                Iterable collection = (Iterable) value;
                for (Object element : collection) {
                    adapt(element, graphModel, values, nodeIdentities, edgeIdentities);
                }
            } else {
                adapt(value, graphModel, values, nodeIdentities, edgeIdentities);
            }
        }
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
}
