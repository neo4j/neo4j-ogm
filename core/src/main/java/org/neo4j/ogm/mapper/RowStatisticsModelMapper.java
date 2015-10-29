package org.neo4j.ogm.mapper;

import org.neo4j.ogm.model.RowStatisticsModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRowStatisticsModel;

import java.util.*;

/**
 * @author vince
 */
public class RowStatisticsModelMapper implements ResponseMapper<RowStatisticsModel> {

    public <T> Iterable<T> map(Class<T> type, Response<RowStatisticsModel> response) {

        RowStatisticsModel result = response.next();
        Set<Map<String, Object>> rowResult = new LinkedHashSet();

        for (Iterator iterator = result.getRows().iterator(); iterator.hasNext(); ) {

            Object[] model =  ((List)iterator.next()).toArray();

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
