package org.neo4j.ogm.driver.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.session.response.model.StatisticsModel;
import org.neo4j.ogm.session.response.model.RowModel;
import org.neo4j.ogm.session.response.model.RowStatisticsModel;

import java.util.Arrays;
import java.util.Map;

/**
 * @author vince
 */
public class RowStatisticsModelResponse extends EmbeddedResponse<RowStatisticsModel> {

    private final RowModelAdapter rowModelAdapter = new RowModelAdapter();
    private final StatisticsModel statisticsModel;

    public RowStatisticsModelResponse(Transaction tx, Result result) {
        super(tx, result);
        statisticsModel = new StatisticsModelAdapter().adapt(result);
    }

    @Override
    public RowStatisticsModel next() {
        if (result.hasNext()) {
            return parse(result.next());
        }
        close();
        return null;
    }

    private RowStatisticsModel parse(Map<String, Object> data) {

        RowStatisticsModel rowStatisticsModel = new RowStatisticsModel();
        RowModel rowModel = rowModelAdapter.adapt(data);

        rowStatisticsModel.setStats(statisticsModel);
        rowStatisticsModel.setRows(Arrays.asList(rowModel.getValues()));

        return rowStatisticsModel;

    }


}
