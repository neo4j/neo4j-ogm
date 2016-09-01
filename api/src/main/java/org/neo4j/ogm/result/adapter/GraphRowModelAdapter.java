/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.result.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowModel;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.response.model.DefaultGraphModel;
import org.neo4j.ogm.response.model.DefaultGraphRowModel;
import org.neo4j.ogm.response.model.DefaultRowModel;

/**
 * This adapter will transform an embedded response into a json response
 *
 * @author vince
 */
public abstract class GraphRowModelAdapter implements ResultAdapter<Map<String, Object>, GraphRowModel> {

	private final GraphModelAdapter graphModelAdapter;
	private List<String> columns = new ArrayList<>();

	public GraphRowModelAdapter(GraphModelAdapter graphModelAdapter) {
		this.graphModelAdapter = graphModelAdapter;
	}


	/**
	 * Reads the next row from the result object and transforms it into a RowModel object
	 *
	 * @param data the data to transform, given as a map
	 * @return @return the data transformed to an {@link RowModel}
	 */
	public GraphRowModel adapt(Map<String, Object> data) {

		if (columns == null) {
			throw new ResultProcessingException("Result columns should not be null");
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

			if (value!=null && value.getClass().isArray()) {
				Iterable<Object> collection = AdapterUtils.convertToIterable(value);
				for (Object element : collection) {
					adapt(element, graphModel, values, nodeIdentities, edgeIdentities);
				}
			} else {
				adapt(value, graphModel, values, nodeIdentities, edgeIdentities);
			}
		}
	}

	private void adapt(Object element, GraphModel graphModel, List<Object> values, Set<Long> nodeIdentities, Set<Long> edgeIdentities) {
		if (graphModelAdapter.isPath(element)) {
			graphModelAdapter.buildPath(element, graphModel, nodeIdentities, edgeIdentities);
		} else if (graphModelAdapter.isNode(element)) {
			graphModelAdapter.buildNode(element, graphModel, nodeIdentities);
		} else if (graphModelAdapter.isRelationship(element)) {
			graphModelAdapter.buildRelationship(element, graphModel, nodeIdentities, edgeIdentities);
		} else {
			values.add(element);
		}
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}
}
