package org.neo4j.ogm.drivers.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.result.ResultAdapter;
import org.neo4j.ogm.transaction.TransactionManager;

import java.util.Map;

/**
 * @author vince
 */
public class GraphModelResponse extends EmbeddedResponse<GraphModel> {

    private final ResultAdapter<Map<String, Object>, GraphModel> adapter = new GraphModelAdapter();

    public GraphModelResponse(Result result, TransactionManager transactionManager) {
        super(result, transactionManager);
    }

    @Override
    public GraphModel next() {
        if (result.hasNext()) {
            return adapter.adapt(result.next());
        }
        return null;
    }

}
