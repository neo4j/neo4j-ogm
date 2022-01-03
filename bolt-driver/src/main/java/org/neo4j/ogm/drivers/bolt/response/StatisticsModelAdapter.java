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
package org.neo4j.ogm.drivers.bolt.response;

import org.neo4j.driver.Result;
import org.neo4j.driver.summary.SummaryCounters;
import org.neo4j.ogm.config.ObjectMapperFactory;
import org.neo4j.ogm.response.model.QueryStatisticsModel;
import org.neo4j.ogm.result.adapter.ResultAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class StatisticsModelAdapter implements ResultAdapter<Result, QueryStatisticsModel> {

    protected static final ObjectMapper mapper = ObjectMapperFactory.objectMapper();

    @Override
    public QueryStatisticsModel adapt(Result result) {
        QueryStatisticsModel queryStatisticsModel = new QueryStatisticsModel();
        SummaryCounters stats = result.consume().counters();
        queryStatisticsModel.setContains_updates(stats.containsUpdates());
        queryStatisticsModel.setNodes_created(stats.nodesCreated());
        queryStatisticsModel.setNodes_deleted(stats.nodesDeleted());
        queryStatisticsModel.setProperties_set(stats.propertiesSet());
        queryStatisticsModel.setRelationships_created(stats.relationshipsCreated());
        queryStatisticsModel.setRelationship_deleted(stats.relationshipsDeleted());
        queryStatisticsModel.setLabels_added(stats.labelsAdded());
        queryStatisticsModel.setLabels_removed(stats.labelsRemoved());
        queryStatisticsModel.setIndexes_added(stats.indexesAdded());
        queryStatisticsModel.setIndexes_removed(stats.indexesRemoved());
        queryStatisticsModel.setConstraints_added(stats.constraintsAdded());
        queryStatisticsModel.setConstraints_removed(stats.constraintsRemoved());
        return queryStatisticsModel;
    }
}
