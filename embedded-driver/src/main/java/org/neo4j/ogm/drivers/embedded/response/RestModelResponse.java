/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.drivers.embedded.response;

import java.util.LinkedHashMap;
import java.util.Map;

import org.neo4j.graphdb.Result;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedEntityAdapter;
import org.neo4j.ogm.model.RestModel;
import org.neo4j.ogm.response.model.DefaultRestModel;
import org.neo4j.ogm.response.model.QueryStatisticsModel;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class RestModelResponse extends EmbeddedResponse<RestModel> {

    private final EmbeddedRestModelAdapter restModelAdapter;
    private final QueryStatisticsModel statisticsModel;

    public RestModelResponse(Result result, TransactionManager transactionManager, EmbeddedEntityAdapter entityAdapter) {

        super(result, transactionManager);

        this.restModelAdapter = new EmbeddedRestModelAdapter(entityAdapter);
        this.statisticsModel = new StatisticsModelAdapter().adapt(result);
    }

    @Override
    public RestModel next() {
        DefaultRestModel defaultRestModel = new DefaultRestModel(buildModel());
        defaultRestModel.setStats(statisticsModel);
        return defaultRestModel;
    }

    private Map<String, Object> buildModel() {
        Map<String, Object> row = new LinkedHashMap<>();
        if (result.hasNext()) {
            Map<String, Object> data = result.next();
            row = restModelAdapter.adapt(data);
        }

        return row;
    }
}
