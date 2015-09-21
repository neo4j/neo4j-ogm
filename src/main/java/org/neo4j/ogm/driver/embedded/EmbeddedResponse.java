package org.neo4j.ogm.driver.embedded;

import org.neo4j.graphdb.Result;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.response.ResponseAdapter;

import java.util.List;

/**
 * @author vince
 */
public class EmbeddedResponse implements Neo4jResponse<String> {

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
        if (result.hasNext()) {
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
        if (type.equals(Neo4jResponse.ResponseRecord.GRAPH)) {
            this.responseAdapter = new GraphResponseAdapter();
        } else {
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


