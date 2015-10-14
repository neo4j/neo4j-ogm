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

package org.neo4j.ogm.api.request;


import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.model.GraphRows;
import org.neo4j.ogm.api.model.Row;
import org.neo4j.ogm.api.model.RowStatistics;
import org.neo4j.ogm.api.response.Response;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public interface Request {

    Response<Graph> execute(GraphModelRequest query);
    Response<Row> execute(RowModelRequest query);
    Response<GraphRows> execute(GraphRowModelRequest query);
    Response<RowStatistics> execute(RowModelStatisticsRequest query);

}
