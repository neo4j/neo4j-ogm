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
package org.neo4j.ogm.drivers.http.response;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.model.RestModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRestModel;
import org.neo4j.ogm.result.ResultRestModel;

/**
 * @author Luanne Misquitta
 */
public class RestModelResponse extends AbstractHttpResponse<ResultRestModel> implements Response<RestModel> {

	private RestModelAdapter restModelAdapter = new RestModelAdapter();

	public RestModelResponse(CloseableHttpResponse httpResponse) throws IOException {
		super(httpResponse.getEntity().getContent(), ResultRestModel.class);
		restModelAdapter.setColumns(columns());
	}

	@Override
	public RestModel next() {
		DefaultRestModel defaultRestModel = new DefaultRestModel(buildModel());
		defaultRestModel.setStats(statistics());
		return defaultRestModel;
	}

	@Override
	public void close() {
		//Nothing to do, the response has been closed already
	}

	private Map<String,Object> buildModel() {
		ResultRestModel result = nextDataRecord("rest");
		Map<String,Object> row = new LinkedHashMap<>();
		if (result != null) {
			row = restModelAdapter.adapt(result.queryResults());
		}

		return row;
	}
}
