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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.json.ObjectMapperFactory;
import org.neo4j.ogm.model.RowStatisticsModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRowStatisticsModel;
import org.neo4j.ogm.result.ResultRowModel;

/**
 * @author vince
 * @author Luanne Misquitta
 */
public class RowStatisticsModelResponse extends AbstractHttpResponse<ResultRowModel> implements Response<RowStatisticsModel> {

	protected static final ObjectMapper mapper = ObjectMapperFactory.objectMapper();

	public RowStatisticsModelResponse(CloseableHttpResponse httpResponse) throws IOException {
		super(httpResponse.getEntity().getContent(), ResultRowModel.class);
	}

	@Override
	public RowStatisticsModel next() {
		DefaultRowStatisticsModel rowQueryStatisticsResult = new DefaultRowStatisticsModel();
		ResultRowModel rowModel = nextDataRecord("row");
		while (rowModel != null) {
			rowQueryStatisticsResult.addRow(rowModel.queryResults());
			rowModel = nextDataRecord("row");
		}
		rowQueryStatisticsResult.setStats(statistics());
		return rowQueryStatisticsResult;
	}

	@Override
	public void close() {
		//Nothing to do, the response has been closed already
	}
}
