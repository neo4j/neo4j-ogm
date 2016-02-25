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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.GraphRowModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultGraphRowListModel;
import org.neo4j.ogm.result.ResultGraphRowListModel;

import java.io.IOException;

/**
 * @author vince
 * @author Luanne Misquitta
 */
public class GraphRowsModelResponse extends AbstractHttpResponse<ResultGraphRowListModel> implements Response<GraphRowListModel> {

	public GraphRowsModelResponse(CloseableHttpResponse httpResponse) throws IOException {
		super(httpResponse, ResultGraphRowListModel.class);
	}

	@Override
	public GraphRowListModel next() {
		ResultGraphRowListModel graphRowModel = nextDataRecord("data");

		if (graphRowModel != null) {
			DefaultGraphRowListModel graphRowListModel = new DefaultGraphRowListModel();
			for (GraphRowModel model : graphRowModel.getData()) {
				graphRowListModel.add(model);
			}
			return graphRowListModel;
		}
		return null;
	}

	@Override
	public void close() {
		//Nothing to do, the response has been closed already
	}
}
