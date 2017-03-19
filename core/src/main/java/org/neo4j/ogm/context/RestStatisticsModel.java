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

package org.neo4j.ogm.context;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.ogm.model.QueryStatistics;

/**
 * @author Luanne Misquitta
 */
public class RestStatisticsModel implements Iterable {

    Collection<Map<String, Object>> result;
    QueryStatistics statistics;

    public Collection<Map<String, Object>> getResult() {
        return result;
    }

    public void setResult(Collection<Map<String, Object>> result) {
        this.result = result;
    }

    public QueryStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(QueryStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public Iterator iterator() {
        return result.iterator();
    }
}
