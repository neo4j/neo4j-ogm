package org.neo4j.ogm.drivers.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.response.model.DefaultRowModel;
import org.neo4j.ogm.result.ResultGraphRowListModel;

import java.util.Map;

/**
 * @author vince
 */
public class GraphRowModelResponse extends EmbeddedResponse<GraphRowListModel> {

    private GraphModelAdapter graphModelAdapter = new GraphModelAdapter();
    private RowModelAdapter rowModelAdapter = new RowModelAdapter();

    public GraphRowModelResponse(Transaction tx, Result result) {
        super(tx, result);
    }

    @Override
    public GraphRowListModel next() {
        if (result.hasNext()) {
            return parse(result.next());
        }
        close();
        return null;
    }

    // this is most likely wrong. we should collect all the results. I don't know why this is different from
    // all the others though.
    private GraphRowListModel parse(Map<String, Object> data) {

        ResultGraphRowListModel graphRowModelResult = new ResultGraphRowListModel();

        // interface
        GraphModel graphModel = graphModelAdapter.adapt(data);

        // class!
        DefaultRowModel rowModel = rowModelAdapter.adapt(data);

        graphRowModelResult.addGraphRowResult(graphModel, rowModel);

        return graphRowModelResult.model();

    }


}
