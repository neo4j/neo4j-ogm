package org.neo4j.ogm.driver.embedded;

import org.neo4j.graphdb.Result;
import org.neo4j.ogm.session.response.Response;
import org.neo4j.ogm.session.response.adapter.ResponseAdapter;

import java.util.List;

/**
 * @author vince
 */
public class EmbeddedResponse implements Response<String> {

    private final Result result;
    private final List<String> columns;
    private int currentRow = 0;

    private ResponseAdapter responseAdapter;

    public EmbeddedResponse(Result result) {
        this.columns = result.columns();
        this.result = result;
        this.expect(ResponseRecord.GRAPH);
    }

    @Override
    public String next() {
        if (result.hasNext() || responseAdapter instanceof RowStatisticsAdapter) {
            currentRow++;
            return (String) responseAdapter.adapt(result);
        }
        close();
        return null;
    }

    @Override
    public void close() {
        result.close();
    }

    @Override
    public void expect(ResponseRecord type) {
        if (type.equals(Response.ResponseRecord.GRAPH)) {
            this.responseAdapter = new GraphModelAdapter();
        }
        else if (type.equals(Response.ResponseRecord.ROW)) {
            this.responseAdapter = new RowModelAdapter();
        }
        else if (type.equals(ResponseRecord.STATS)) {
            this.responseAdapter = new RowStatisticsAdapter();
        }
        else {
            throw new RuntimeException("Transform not implemented: " + type);
        }
    }

    @Override
    public String[] columns() {
        return columns.toArray(new String[]{});
    }

    @Override
    public int rowId() {
        return currentRow;
    }

}


