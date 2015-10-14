package org.neo4j.ogm.driver.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.api.result.ResultAdapter;
import org.neo4j.ogm.driver.impl.model.RowModel;

import java.util.Map;

/**
 * @author vince
 */
public class RowModelResponse extends EmbeddedResponse<RowModel> {

    private final ResultAdapter<Map<String, Object>, RowModel> adapter = new RowModelAdapter();

    public RowModelResponse(Transaction tx, Result result) {
        super(tx, result);
        ((RowModelAdapter) adapter).setColumns(result.columns());
    }

    @Override
    public RowModel next() {
        if (result.hasNext()) {
            return adapter.adapt(result.next());
        }
        close();
        return null;
    }

}
