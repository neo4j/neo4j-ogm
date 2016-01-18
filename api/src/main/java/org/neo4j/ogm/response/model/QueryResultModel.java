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

package org.neo4j.ogm.response.model;

import org.neo4j.ogm.model.QueryStatistics;
import org.neo4j.ogm.model.Result;

import java.util.Iterator;
import java.util.Map;

/**
 * @author vince
 */
public class QueryResultModel implements Result {

    private final Iterable<Map<String,Object>> result;
    private final QueryStatistics queryQueryStatistics;

    public QueryResultModel(Iterable<Map<String, Object>> result, QueryStatistics queryQueryStatistics) {
        this.result = result;
        this.queryQueryStatistics = queryQueryStatistics;
    }

    @Override
    public Iterator<Map<String,Object>> iterator() {
        return result.iterator();
    }

    @Override
    public Iterable<Map<String,Object>> queryResults() {
        return result;
    }

    @Override
    public QueryStatistics queryStatistics() {
        return queryQueryStatistics;
    }
}
