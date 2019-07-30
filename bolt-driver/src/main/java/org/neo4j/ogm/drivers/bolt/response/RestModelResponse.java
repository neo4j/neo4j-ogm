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
package org.neo4j.ogm.drivers.bolt.response;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.neo4j.driver.Record;
import org.neo4j.driver.StatementResult;
import org.neo4j.ogm.drivers.bolt.driver.BoltEntityAdapter;
import org.neo4j.ogm.model.QueryStatistics;
import org.neo4j.ogm.model.RestModel;
import org.neo4j.ogm.response.model.DefaultRestModel;
import org.neo4j.ogm.response.model.QueryStatisticsModel;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class RestModelResponse extends BoltResponse<RestModel> {

    private BoltRestModelAdapter restModelAdapter;
    private final QueryStatisticsModel statisticsModel;
    private final Iterator<Record> resultProjection;

    public RestModelResponse(StatementResult result, BoltEntityAdapter entityAdapter) {

        super(result);

        this.restModelAdapter = new BoltRestModelAdapter(entityAdapter);
        this.resultProjection = result.list().iterator();
        this.statisticsModel = new StatisticsModelAdapter().adapt(result);
    }

    @Override
    public RestModel fetchNext() {
        return DefaultRestModel.basedOn(buildModel())
            .orElse(null);
    }

    private Map<String, Object> buildModel() {
        Map<String, Object> row = new LinkedHashMap<>();
        if (resultProjection.hasNext()) {
            row = restModelAdapter.adapt(resultProjection.next().asMap());
        }

        return row;
    }

    @Override
    public Optional<QueryStatistics> getStatistics() {
        return Optional.of(statisticsModel);
    }
}
