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

package org.neo4j.ogm.session.response;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.neo4j.ogm.session.result.CypherException;
import org.neo4j.ogm.session.result.QueryStatistics;
import org.neo4j.ogm.session.result.RowQueryStatisticsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luanne Misquitta
 */
public class RowStatisticsResponse implements Neo4jResponse<RowQueryStatisticsResult> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RowStatisticsResponse.class);

	private final ObjectMapper objectMapper;
	private final Neo4jResponse<String> response;
	private String[] columns;

	public RowStatisticsResponse(Neo4jResponse<String> response, ObjectMapper mapper) {
		this.response = response;
		this.objectMapper = mapper;
		try {
			initialiseScan(ResponseRecord.RESULTS);
		}
		catch ( CypherException ce ) {
			close();
			throw ce;
		} catch (Exception e) {
			//Ignore this exception since we're reading the JSON manually in next()
			//TODO look into enhancing the JSONResponse parsing
		}
	}
	@Override
	public RowQueryStatisticsResult next() {
		String json = response.next();
		if (json != null) {
			try {
				RowQueryStatisticsResult rowQueryStatisticsResult = new RowQueryStatisticsResult();
				JSONObject jsonObject = getOuterObject(json);
				JSONArray columnsObject = jsonObject.getJSONArray("results").getJSONObject(0).getJSONArray("columns");
				columns = objectMapper.readValue(columnsObject.toString(), String[].class);
				JSONArray dataObject = jsonObject.getJSONArray("results").getJSONObject(0).getJSONArray("data");
				JSONObject statsJson = jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("stats");
				rowQueryStatisticsResult.setStats(objectMapper.readValue(statsJson.toString(),QueryStatistics.class));
				List<Object> rows = new ArrayList<>();
				for (int i = 0; i < dataObject.length(); i++) {
					String rowJson = dataObject.getJSONObject(i).getString("row");
					Object row = objectMapper.readValue(rowJson, Object.class);
					rows.add(row);
				}
				rowQueryStatisticsResult.setRows(rows);
				return rowQueryStatisticsResult;
			} catch (Exception e) {
				LOGGER.error("failed to parse: {}", json);
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}

	@Override
	public void close() {
		response.close();
	}

	@Override
	public void initialiseScan(ResponseRecord record) {
		response.initialiseScan(record);
	}

	@Override
	public String[] columns() {
		return columns;
	}

	@Override
	public int rowId() {
		return -1;
	}

	private JSONObject getOuterObject(String json) throws JSONException {
		JSONObject outerObject;
		try {
			outerObject = new JSONObject(json);
		} catch (JSONException e) {
			outerObject = new JSONObject(json + "]}"); //TODO enhance the JSONParser to not strip off these 2 characters
		}
		return outerObject;
	}
}
