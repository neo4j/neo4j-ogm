package org.neo4j.ogm.drivers.bolt.response;

import org.neo4j.driver.Result;
import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.response.Response;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author vince
 */
public class GraphModelResponse implements Response<Graph> {

    private final Result result;
    private final GraphModelAdapter adapter = new GraphModelAdapter();
    private int rowId = 0;

    public GraphModelResponse(Result result) {
        this.result = result;
    }

    @Override
    public Graph next() {
        if (result.next()) {
            rowId++;
            return adapter.adapt(result).model();
        }
        return null;
    }

    @Override
    public void close() {
        while (result.next()); // is this the only way to close a result? or do we not need to ?
    }

    @Override
    public String[] columns() {
        List<String> copy = toList(result.fieldNames().iterator());
        return copy.toArray(new String[copy.size()]);
    }

    private <T> List<T> toList(Iterator<T> iterator) {
        List<T> copy = new ArrayList<T>();
        while (iterator.hasNext()) {
            copy.add(iterator.next());
        }
        return copy;
    }
}
