package org.neo4j.ogm.drivers.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.response.model.DefaultRowStatisticsModel;
import org.neo4j.ogm.response.model.StatisticsModel;
import org.neo4j.ogm.result.ResultRowModel;
import org.neo4j.ogm.transaction.TransactionManager;

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
        rowModelAdapter.setColumns(result.columns());
    }

    @Override
    public DefaultRowStatisticsModel next() {
        DefaultRowStatisticsModel rowQueryStatisticsResult = new DefaultRowStatisticsModel();
        ResultRowModel rowModel = parse();
        while (rowModel != null) {
            rowQueryStatisticsResult.addRow(rowModel.model());
            rowModel = parse();
        }
        rowQueryStatisticsResult.setStats(statisticsModel);
        return rowQueryStatisticsResult;
    }

    private ResultRowModel parse() {
        if (result.hasNext()) {
            ResultRowModel model = new ResultRowModel();
            Map<String, Object> data = result.next();
            RowModel rowModel = rowModelAdapter.adapt(data);
            model.setRow(rowModel.getValues());
            return model;
        }
        return null;
    }

}
