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

package org.neo4j.ogm.drivers.embedded.response;

import java.util.LinkedHashMap;
import java.util.Map;

import org.neo4j.graphdb.Result;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedEntityAdapter;
import org.neo4j.ogm.response.model.DefaultRestModel;
import org.neo4j.ogm.response.model.QueryStatisticsModel;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class RestModelResponse extends EmbeddedResponse<DefaultRestModel> {

    private final EmbeddedRestModelAdapter restModelAdapter;
    private final QueryStatisticsModel statisticsModel;

    public RestModelResponse(Result result, TransactionManager transactionManager, EmbeddedEntityAdapter entityAdapter) {

        super(result, transactionManager);

        this.restModelAdapter = new EmbeddedRestModelAdapter(entityAdapter);
        this.statisticsModel = new StatisticsModelAdapter().adapt(result);
    }

    @Override
    public DefaultRestModel next() {
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
