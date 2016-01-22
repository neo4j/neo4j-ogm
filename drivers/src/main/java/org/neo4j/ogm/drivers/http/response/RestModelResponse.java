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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.model.RestModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRestModel;
import org.neo4j.ogm.result.ResultRestModel;

/**
 * @author Luanne Misquitta
 */
public class RestModelResponse extends AbstractHttpResponse<ResultRestModel> implements Response<RestModel> {

	public RestModelResponse(CloseableHttpResponse httpResponse) throws IOException {
		super(httpResponse.getEntity().getContent(), ResultRestModel.class);
	}

	@Override
	public RestModel next() {
		ResultRestModel restModel = nextDataRecord("rest");

		if (restModel != null) {
			DefaultRestModel model = new DefaultRestModel(restModel.queryResults());
			model.setStats(statistics());
			return model;
		}
		DefaultRestModel model = new DefaultRestModel(null);
		model.setStats(statistics());
		return model;
	}

	@Override
	public void close() {
		//Nothing to do, the response has been closed already
	}
}
