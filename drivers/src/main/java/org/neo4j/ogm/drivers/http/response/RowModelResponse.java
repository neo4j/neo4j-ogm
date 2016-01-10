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

package org.neo4j.ogm.drivers.http.response;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRowModel;
import org.neo4j.ogm.result.ResultRowModel;

/**
 * @author vince
 * @author Luanne Misquitta
 */
public class RowModelResponse extends AbstractHttpResponse<ResultRowModel> implements Response<RowModel> {

	public RowModelResponse(CloseableHttpResponse httpResponse) throws IOException {
		super(httpResponse.getEntity().getContent(), ResultRowModel.class);
	}

	@Override
	public RowModel next() {
		ResultRowModel rowModel = nextDataRecord("row");

		if (rowModel != null) {
			return new DefaultRowModel(rowModel.queryResults(), columns());
		}
		return null;
	}

	@Override
	public void close() {
		//Nothing to do, the response has been closed already
	}
}
