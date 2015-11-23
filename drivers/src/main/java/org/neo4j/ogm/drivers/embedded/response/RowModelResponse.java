package org.neo4j.ogm.drivers.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.result.ResultAdapter;
import org.neo4j.ogm.transaction.TransactionManager;

import java.util.Map;

/**
 * @author vince
 */
public class RowModelResponse extends EmbeddedResponse<RowModel> {

    private final ResultAdapter<Map<String, Object>, RowModel> adapter = new RowModelAdapter();

    public RowModelResponse(Result result, TransactionManager transactionManager) {
        super(result, transactionManager);
        ((RowModelAdapter) adapter).setColumns(result.columns());
    }

    @Override
    public RowModel next() {
        if (result.hasNext()) {
            return adapter.adapt(result.next());
        }
        return null;
    }

}
