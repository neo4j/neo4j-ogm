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

package org.neo4j.ogm.drivers.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.response.model.QueryStatisticsModel;
import org.neo4j.ogm.result.ResultAdapter;

/**
 * @author vince
 */
public class StatisticsModelAdapter extends JsonAdapter implements ResultAdapter<Result, QueryStatisticsModel> {


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

            return mapper.readValue(stats,QueryStatisticsModel.class);

        } catch (Exception e) {
            throw new ResultProcessingException("Could not read response statistics", e);
        }
    }
}
