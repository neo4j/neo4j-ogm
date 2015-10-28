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

package org.neo4j.ogm.session.response.handler;

import org.neo4j.ogm.compiler.CompileContext;
import org.neo4j.ogm.mapper.RowMapper;
import org.neo4j.ogm.model.*;
import org.neo4j.ogm.response.Response;

import java.util.Collection;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public interface ResponseHandler {

    <T> Collection<T> loadGraphResponse(Class<T> type, Response<Graph> stream);
    <T> Collection<T> loadRowResponse(Class<T> type, Response<Row> response, RowMapper mapper);


    QueryResult loadQueryResult(Response<RowStatistics> response);

    <T> Collection<T> loadByProperty(Class<T> type, Response<GraphRows> stream);
    <T> T loadById(Class<T> type, Response<Graph> stream, Long id);


    void updateObjects(CompileContext context, Response<Row> response);

}
