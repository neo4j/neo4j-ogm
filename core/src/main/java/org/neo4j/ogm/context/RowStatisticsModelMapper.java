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
package org.neo4j.ogm.context;

import org.neo4j.ogm.model.RowStatisticsModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRowStatisticsModel;

import java.util.*;

/**
 * @author vince
 */
public class RowStatisticsModelMapper implements ResponseMapper<RowStatisticsModel> {

    public <T> Iterable<T> map(Class<T> type, Response<RowStatisticsModel> response) {


        DefaultRowStatisticsModel rowStatisticsModel = new DefaultRowStatisticsModel();

        RowStatisticsModel result = response.next();
        Set<Map<String, Object>> rowResult = new LinkedHashSet();

        if (result.getRows() != null) {
            // each row in the response is a sequence of data values, passed in an Object[] array
            for (Iterator<Object[]> iterator = result.getRows().iterator(); iterator.hasNext(); ) {

                Object[] model = iterator.next();
                // for each element in the data array, put it and its name into a map.
                Map<String, Object> element = new HashMap<>();
                for (int i = 0; i < model.length; i++) {
                    element.put(response.columns()[i], model[i]);
                }

                // add the map to the result
                rowResult.add(element);
            }
            rowStatisticsModel.setRows(rowResult);
        }

        rowStatisticsModel.setStats(result.getStats());

        return (Iterable<T>) rowStatisticsModel;

    }
}
