/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.drivers.bolt.response;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.summary.SummaryCounters;
import org.neo4j.ogm.config.ObjectMapperFactory;
import org.neo4j.ogm.response.model.QueryStatisticsModel;
import org.neo4j.ogm.result.adapter.ResultAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Luanne Misquitta
 */
public class StatisticsModelAdapter implements ResultAdapter<StatementResult, QueryStatisticsModel> {

    protected static final ObjectMapper mapper = ObjectMapperFactory.objectMapper();

    @Override
    public QueryStatisticsModel adapt(StatementResult result) {
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
