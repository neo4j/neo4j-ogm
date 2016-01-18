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
import org.neo4j.ogm.model.RowStatisticsModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Encapsulates {@link QueryStatisticsModel} and row data returned by a query.
 * @author Luanne Misquitta
 */
public class DefaultRowStatisticsModel implements RowStatisticsModel {

    private Collection<Object[]> rows = new ArrayList<>();
    private QueryStatistics stats = new QueryStatisticsModel();

    public QueryStatistics getStats() {
        return stats;
    }

    public void setRows(Collection rows) {
        this.rows = rows;
    }

    public void setStats(QueryStatistics stats) {
        this.stats = stats;
    }

    public void addRow(Object[] row) {
        rows.add(row);
    }

    public Collection<Object[]> getRows() {
        return rows;
    }

    @Override
    public Iterator iterator() {
        return rows.iterator();
    }
}
