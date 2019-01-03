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

import org.neo4j.graphdb.Result;
import org.neo4j.ogm.config.ObjectMapperFactory;
import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.response.model.QueryStatisticsModel;
import org.neo4j.ogm.result.adapter.ResultAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author vince
 */
public class StatisticsModelAdapter implements ResultAdapter<Result, QueryStatisticsModel> {

    protected static final ObjectMapper mapper = ObjectMapperFactory.objectMapper();

    @Override
    public QueryStatisticsModel adapt(Result response) {
        try {
            org.neo4j.graphdb.QueryStatistics statistics = response.getQueryStatistics();
            String stats = mapper.writeValueAsString(statistics);
            stats = stats.replace("Deleted", "_deleted");
            stats = stats.replace("Added", "_added");
            stats = stats.replace("Updates", "_updates");
            stats = stats.replace("Created", "_created");
            stats = stats.replace("Set", "_set");
            stats = stats.replace("Removed", "_removed");
            stats = stats.replace("deletedNodes", "nodes_deleted");
            stats = stats.replace("deletedRelationships", "relationships_deleted");

            //Modify the string to include contains_updates as it is a calculated value
            String containsUpdates = ",\"contains_updates\":" + statistics.containsUpdates();
            int closingBraceIndex = stats.lastIndexOf("}");
            stats = stats.substring(0, closingBraceIndex) + containsUpdates + "}";

            return mapper.readValue(stats, QueryStatisticsModel.class);
        } catch (Exception e) {
            throw new ResultProcessingException("Could not read response statistics", e);
        }
    }
}
