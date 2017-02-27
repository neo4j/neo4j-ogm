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

package org.neo4j.ogm.drivers.bolt.response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowModel;
import org.neo4j.ogm.response.model.DefaultGraphModel;
import org.neo4j.ogm.response.model.DefaultGraphRowModel;
import org.neo4j.ogm.response.model.DefaultRowModel;
import org.neo4j.ogm.result.adapter.AdapterUtils;
import org.neo4j.ogm.result.adapter.GraphRowModelAdapter;

/**
 * @author Luanne Misquitta
 */
public class BoltGraphRowModelAdapter extends GraphRowModelAdapter {

	private List<String> columns = new ArrayList<>();

	public BoltGraphRowModelAdapter(BoltGraphModelAdapter graphModelAdapter) {
		super(graphModelAdapter);
	}

	@Override
	public GraphRowModel adapt(Map<String, Object> data) {
		if (columns == null) {
			throw new ResultProcessingException("Column data cannot be null!");
		}

		Set<Long> nodeIdentities = new HashSet<>();
		Set<Long> edgeIdentities = new HashSet<>();

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
				Iterable<Object> collection = AdapterUtils.convertToIterable(value);
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
