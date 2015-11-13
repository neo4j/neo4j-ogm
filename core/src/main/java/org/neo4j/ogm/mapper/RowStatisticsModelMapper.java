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
package org.neo4j.ogm.mapper;

import java.util.*;

import org.neo4j.ogm.model.RowStatisticsModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRowStatisticsModel;

/**
 * @author vince
 */
public class RowStatisticsModelMapper implements ResponseMapper<RowStatisticsModel> {

    public <T> Iterable<T> map(Class<T> type, Response<RowStatisticsModel> response) {

        RowStatisticsModel result = response.next();
        Set<Map<String, Object>> rowResult = new LinkedHashSet();

        for (Iterator<Object[]> iterator = result.getRows().iterator(); iterator.hasNext(); ) {

            Object[] model =  iterator.next();

            Map<String, Object> element = new HashMap<>();
            for (int i = 0; i < model.length; i++) {
                element.put(response.columns()[i], model[i]);
            }

            rowResult.add(element);
        }

        DefaultRowStatisticsModel rowStatisticsModel = new DefaultRowStatisticsModel();
        rowStatisticsModel.setRows(rowResult);
        rowStatisticsModel.setStats(result.getStats());
        return (Iterable<T>) rowStatisticsModel;
    }
}
