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
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.response.model.DefaultRowStatisticsModel;
import org.neo4j.ogm.response.model.QueryStatisticsModel;
import org.neo4j.ogm.result.ResultRowModel;
import org.neo4j.ogm.transaction.TransactionManager;

import java.util.Map;

/**
 * @author vince
 */
public class RowStatisticsModelResponse extends EmbeddedResponse<DefaultRowStatisticsModel> {

    private final RowModelAdapter rowModelAdapter = new RowModelAdapter();
    private final QueryStatisticsModel statisticsModel;

    public RowStatisticsModelResponse(Result result, TransactionManager transactionManager) {
        super(result, transactionManager);
        statisticsModel = new StatisticsModelAdapter().adapt(result);
        rowModelAdapter.setColumns(result.columns());
    }

    @Override
    public DefaultRowStatisticsModel next() {
        DefaultRowStatisticsModel rowQueryStatisticsResult = new DefaultRowStatisticsModel();
        ResultRowModel rowModel = parse();
        while (rowModel != null) {
            rowQueryStatisticsResult.addRow(rowModel.queryResults());
            rowModel = parse();
        }
        rowQueryStatisticsResult.setStats(statisticsModel);
        return rowQueryStatisticsResult;
    }

    private ResultRowModel parse() {
        if (result.hasNext()) {
            ResultRowModel model = new ResultRowModel();
            Map<String, Object> data = result.next();
            RowModel rowModel = rowModelAdapter.adapt(data);
            model.setRow(rowModel.getValues());
            return model;
        }
        return null;
    }

}
