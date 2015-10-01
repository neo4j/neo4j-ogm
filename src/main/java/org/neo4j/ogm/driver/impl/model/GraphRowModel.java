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

package org.neo4j.ogm.driver.impl.model;

import org.neo4j.ogm.driver.impl.result.ResultGraphRow;

import java.util.ArrayList;
import java.util.List;

/**
 * The results of a query, modelled as both both graph and row data.
 *
 * @author Luanne Misquitta
 */
public class GraphRowModel {

	List<ResultGraphRow> graphRowResults = new ArrayList<>();

	public List<ResultGraphRow> getGraphRowResults() {
		return graphRowResults;
	}

    public void addGraphRowResult(GraphModel graphModel, RowModel rowModel) {
        graphRowResults.add(new ResultGraphRow(graphModel, rowModel.getValues()));
    }

	public void addGraphRowResult(GraphModel graphModel, Object[] rowModel) {
		graphRowResults.add(new ResultGraphRow(graphModel, rowModel));
	}
}
