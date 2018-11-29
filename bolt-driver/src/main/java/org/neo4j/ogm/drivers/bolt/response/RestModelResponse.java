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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.ogm.drivers.bolt.driver.BoltEntityAdapter;
import org.neo4j.ogm.response.model.DefaultRestModel;
import org.neo4j.ogm.response.model.QueryStatisticsModel;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class RestModelResponse extends BoltResponse<DefaultRestModel> {

    private BoltRestModelAdapter restModelAdapter;
    private final QueryStatisticsModel statisticsModel;
    private final Iterator<Record> resultProjection;

    public RestModelResponse(StatementResult result, TransactionManager transactionManager, BoltEntityAdapter entityAdapter) {

        super(result, transactionManager);

        this.restModelAdapter = new BoltRestModelAdapter(entityAdapter);
        this.resultProjection = result.list().iterator();
        this.statisticsModel = new StatisticsModelAdapter().adapt(result);
    }

    @Override
    public DefaultRestModel fetchNext() {
        DefaultRestModel defaultRestModel = new DefaultRestModel(buildModel());
        defaultRestModel.setStats(statisticsModel);
        return defaultRestModel;
    }

    private Map<String, Object> buildModel() {
        Map<String, Object> row = new LinkedHashMap<>();
        if (resultProjection.hasNext()) {
            row = restModelAdapter.adapt(resultProjection.next().asMap());
        }

        return row;
    }
}
