/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.drivers.http.response;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.model.QueryStatistics;
import org.neo4j.ogm.model.RestModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRestModel;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class RestModelResponse extends AbstractHttpResponse<Object[]> implements Response<RestModel> {

    private RestModelAdapter restModelAdapter = new RestModelAdapter();

    public RestModelResponse(CloseableHttpResponse httpResponse) {
        super(httpResponse, Object[].class);
        restModelAdapter.setColumns(columns());
    }

    @Override
    public RestModel next() {

        return DefaultRestModel.basedOn(buildModel())
            .orElse(null);
    }

    private Map<String, Object> buildModel() {
        Object[] result = nextDataRecord("rest");
        Map<String, Object> row = new LinkedHashMap<>();
        if (result != null) {
            row = restModelAdapter.adapt(result);
        }

        return row;
    }

    @Override
    public Optional<QueryStatistics> getStatistics() {
        return Optional.of(statistics());
    }
}
