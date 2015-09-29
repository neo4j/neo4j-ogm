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

package org.neo4j.ogm.session.request;


import org.neo4j.ogm.cypher.query.GraphModelRequest;
import org.neo4j.ogm.cypher.query.GraphRowModelRequest;
import org.neo4j.ogm.cypher.query.RowModelRequest;
import org.neo4j.ogm.cypher.query.RowModelStatisticsRequest;
import org.neo4j.ogm.session.response.Response;
import org.neo4j.ogm.session.response.model.GraphModel;
import org.neo4j.ogm.session.response.model.GraphRowModel;
import org.neo4j.ogm.session.response.model.RowModel;
import org.neo4j.ogm.session.response.model.RowStatisticsModel;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public interface Request {

    Response<GraphModel> execute(GraphModelRequest query);
    Response<RowModel> execute(RowModelRequest query);
    Response<GraphRowModel> execute(GraphRowModelRequest query);
    Response<RowStatisticsModel> execute(RowModelStatisticsRequest query);

    //Response<String> execute(ParameterisedStatement statement);
}
