package org.neo4j.ogm.driver.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.session.response.model.GraphModel;
import org.neo4j.ogm.session.response.model.GraphRowModel;
import org.neo4j.ogm.session.response.model.RowModel;

import java.util.Map;

/**
 * @author vince
 */
public class GraphRowModelResponse extends EmbeddedResponse<GraphRowModel> {

    private GraphModelAdapter graphModelAdapter = new GraphModelAdapter();
    private RowModelAdapter rowModelAdapter = new RowModelAdapter();

    public GraphRowModelResponse(Transaction tx, Result result) {
        super(tx, result);
    }

    @Override
    public GraphRowModel next() {
        if (result.hasNext()) {
            return parse(result.next());
        }
        close();
        return null;
    }

    private GraphRowModel parse(Map<String, Object> data) {

        GraphRowModel graphRowModel = new GraphRowModel();

        GraphModel graphModel = graphModelAdapter.adapt(data);
        RowModel rowModel = rowModelAdapter.adapt(data);

        graphRowModel.addGraphRowResult(graphModel, rowModel);

        return graphRowModel;

    }


}
