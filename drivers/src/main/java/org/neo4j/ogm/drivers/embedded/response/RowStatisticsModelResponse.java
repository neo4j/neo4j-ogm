package org.neo4j.ogm.drivers.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.response.model.DefaultRowStatisticsModel;
import org.neo4j.ogm.response.model.StatisticsModel;
import org.neo4j.ogm.transaction.TransactionManager;

import java.util.Arrays;
import java.util.Map;

/**
 * @author vince
 */
public class RowStatisticsModelResponse extends EmbeddedResponse<DefaultRowStatisticsModel> {

    private final RowModelAdapter rowModelAdapter = new RowModelAdapter();
    private final StatisticsModel statisticsModel;

    public RowStatisticsModelResponse(Result result, TransactionManager transactionManager) {
        super(result, transactionManager);
        statisticsModel = new StatisticsModelAdapter().adapt(result);
    }

    @Override
    public DefaultRowStatisticsModel next() {
        if (result.hasNext()) {
            return parse(result.next());
        }
        //close();
        return null;
    }

    private DefaultRowStatisticsModel parse(Map<String, Object> data) {

        DefaultRowStatisticsModel rowStatisticsModel = new DefaultRowStatisticsModel();
        RowModel rowModel = rowModelAdapter.adapt(data);

        rowStatisticsModel.setStats(statisticsModel);
        rowStatisticsModel.setRows(Arrays.asList(rowModel.getValues()));

        return rowStatisticsModel;

    }


}
