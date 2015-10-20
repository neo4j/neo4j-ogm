package org.neo4j.ogm.drivers.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.model.GraphRows;
import org.neo4j.ogm.api.response.model.RowModel;
import org.neo4j.ogm.api.result.ResultGraphRowsModel;

import java.util.Map;

/**
 * @author vince
 */
public class GraphRowModelResponse extends EmbeddedResponse<GraphRows> {

    private GraphModelAdapter graphModelAdapter = new GraphModelAdapter();
    private RowModelAdapter rowModelAdapter = new RowModelAdapter();

    public GraphRowModelResponse(Transaction tx, Result result) {
        super(tx, result);
    }

    @Override
    public GraphRows next() {
        if (result.hasNext()) {
            return parse(result.next());
        }
        close();
        return null;
    }

    // this is most likely wrong. we should collect all the results. I don't know why this is different from
    // all the others though.
    private GraphRows parse(Map<String, Object> data) {

        ResultGraphRowsModel graphRowModelResult = new ResultGraphRowsModel();

        Graph graphModel = graphModelAdapter.adapt(data);
        RowModel rowModel = rowModelAdapter.adapt(data);

        graphRowModelResult.addGraphRowResult(graphModel, rowModel);

        return graphRowModelResult.model();

    }


}
