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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.ogm.session.response.model.GraphModel;
import org.neo4j.ogm.session.response.model.GraphRowModel;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;

/**
 * The {@link Response} that contains data in both graph and row formats.
 *
 * These responses should be identical, whichever driver is being used to provide them
 *
 *
 * @author Luanne Misquitta
 */
public class GraphRowModelResponse implements Response<GraphRowModel> {

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphRowModelResponse.class);

	private final ObjectMapper objectMapper;
	private final Response<String> response;

	public GraphRowModelResponse(Response<String> response, ObjectMapper objectMapper) {
		this.response = response;
		this.objectMapper = objectMapper;
		try {
			expect(ResponseRecord.RESULTS);
		} catch (Exception e) {
			//Ignore this exception since we're reading the JSON manually in next()
			//TODO look into enhancing the JSONResponse parsing
		}
	}

	@Override
	public GraphRowModel next() {
		String json = response.next();
		if (json != null) {
			try {
				GraphRowModel graphRowModel = new GraphRowModel();
				JSONObject jsonObject = getOuterObject(json);
				JSONArray dataObject = jsonObject.getJSONArray("results").getJSONObject(0).getJSONArray("data");
				for (int i = 0; i < dataObject.length(); i++) {
					String graphJson = dataObject.getJSONObject(i).getString("graph");
					String rowJson = dataObject.getJSONObject(i).getString("row");
					GraphModel graphModel = objectMapper.readValue(graphJson, GraphModel.class);
					Object[] rows = objectMapper.readValue(rowJson, Object[].class);
					graphRowModel.addGraphRowResult(graphModel, rows);
				}
				return graphRowModel;
			} catch (Exception e) {
				LOGGER.error("failed to parse: " + json);
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
	public void expect(ResponseRecord record) {
		response.expect(record);
	}

	@Override
	public String[] columns() {
		return response.columns();
	}

	@Override
	public int rowId() {
		return response.rowId();
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

    /**
     * Represents a single row in a query response which returns both graph and row data.
     *
     * @author Luanne Misquitta
     */
    public static class GraphRowResult {

        private GraphModel graph;
        private Object[] row;

        public GraphRowResult(GraphModel graph, Object[] row) {
            this.graph = graph;
            this.row = row;
        }

        public GraphModel getGraph() {
            return graph;
        }

        public Object[] getRow() {
            return row;
        }
    }
}
