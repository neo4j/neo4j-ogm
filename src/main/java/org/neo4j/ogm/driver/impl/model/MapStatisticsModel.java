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

import org.neo4j.ogm.api.model.QueryStatistics;
import org.neo4j.ogm.api.model.Statistics;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Luanne Misquitta
 */
public class MapStatisticsModel implements QueryStatistics {

	private Iterable<Map<String,Object>> result;
	private org.neo4j.ogm.api.model.Statistics queryStatistics;

	public MapStatisticsModel(Iterable<Map<String, Object>> result, Statistics queryStatistics) {
		this.result = result;
		this.queryStatistics = queryStatistics;
	}

	@Override
	public Iterator<Map<String,Object>> iterator() {
		return result.iterator();
	}

	@Override
	public Iterable<Map<String,Object>> model() {
		return result;
	}

	@Override
	public Statistics statistics() {
		return queryStatistics;
	}
}
