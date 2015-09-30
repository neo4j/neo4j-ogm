package org.neo4j.ogm.driver.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.session.response.model.GraphModel;
import org.neo4j.ogm.driver.ResultAdapter;

import java.util.Map;

/**
 * @author vince
 */
public class GraphModelResponse extends EmbeddedResponse<GraphModel> {

    private final ResultAdapter<Map<String, Object>, GraphModel> adapter = new GraphModelAdapter();

    public GraphModelResponse(Transaction tx, Result result) {
        super(tx, result);
    }

    @Override
    public GraphModel next() {
        if (result.hasNext()) {
            return adapter.adapt(result.next());
        }
        close();
        return null;
    }

}
