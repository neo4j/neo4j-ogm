/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.drivers.embedded.response;

import java.util.Map;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.response.model.DefaultGraphRowModel;
import org.neo4j.ogm.result.ResultGraphRowListModel;

/**
 * @author vince
 */
public class GraphRowModelResponse extends EmbeddedResponse<GraphRowListModel> {

    private GraphModelAdapter graphModelAdapter = new GraphModelAdapter();
    private RowModelAdapter rowModelAdapter = new RowModelAdapter();

    public GraphRowModelResponse(Transaction tx, Result result) {
        super(tx, result);
        rowModelAdapter.setColumns(result.columns());
    }

    @Override
    public GraphRowListModel next() {
        if (result.hasNext()) {
            return parse(result.next());
        }
        close();
        return null;
    }

    // this is most likely wrong. we should collect all the results. I don't know why this is different from
    // all the others though.
    private GraphRowListModel parse(Map<String, Object> data) {

        ResultGraphRowListModel graphRowModelResult = new ResultGraphRowListModel();

        GraphModel graphModel = graphModelAdapter.adapt(data);
        RowModel rowModel = rowModelAdapter.adapt(data);

        graphRowModelResult.addGraphRowResult(new DefaultGraphRowModel(graphModel, rowModel.getValues()));

        return graphRowModelResult.model();

    }


}
