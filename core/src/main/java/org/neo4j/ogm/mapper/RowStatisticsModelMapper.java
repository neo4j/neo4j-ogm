package org.neo4j.ogm.mapper;

import org.neo4j.ogm.model.RowStatisticsModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRowStatisticsModel;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author vince
 */
public class RowStatisticsModelMapper implements Mapper<Response<RowStatisticsModel>> {

    public <T> Iterable<T> map(Class<T> type, Response<RowStatisticsModel> response) {

        RowMapper mapper = new MapRowModelMapper();

        RowStatisticsModel result = response.next();
        Collection rowResult = new LinkedHashSet();

        for (Iterator iterator = result.getRows().iterator(); iterator.hasNext(); ) {
            List next =  (List) iterator.next();
            mapper.map(rowResult, next.toArray(), response.columns());
        }

        DefaultRowStatisticsModel rowStatisticsModel = new DefaultRowStatisticsModel();
        rowStatisticsModel.setRows(rowResult);
        rowStatisticsModel.setStats(result.getStats());
        return (Iterable<T>) rowStatisticsModel;
    }
}
