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

package org.neo4j.ogm.result;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.GraphRowModel;
import org.neo4j.ogm.model.Query;
import org.neo4j.ogm.response.model.DefaultGraphRowListModel;
import org.neo4j.ogm.response.model.DefaultGraphRowModel;

/**
 *  The results of a query, modelled as a collection of GraphRow objects (both both graph and row data).
 *
 * @author Luanne Misquitta
 */
public class ResultGraphRowListModel implements Query<GraphRowListModel> {

    private List<DefaultGraphRowModel> data = new ArrayList<>();


    private final DefaultGraphRowListModel model;

    public ResultGraphRowListModel() {
        model = new DefaultGraphRowListModel();
    }

    public GraphRowListModel queryResults() {
        DefaultGraphRowListModel graphRowListModel = new DefaultGraphRowListModel();
        graphRowListModel.addAll(data);
        return graphRowListModel;
    }

    public void addGraphRowResult(GraphRowModel graphRowModel) {
        model.add(graphRowModel);
    }

    public List<DefaultGraphRowModel> getData() {
        return data;
    }

    public void setData(List<DefaultGraphRowModel> data) {
        this.data = data;
    }
}
