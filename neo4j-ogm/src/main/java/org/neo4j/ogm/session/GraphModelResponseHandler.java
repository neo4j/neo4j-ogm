package org.neo4j.ogm.session;

import org.graphaware.graphmodel.neo4j.GraphModel;

public class GraphModelResponseHandler implements Neo4jResponseHandler<GraphModel> {

    private final GraphModelResult[] results;
    private int count;

    public GraphModelResponseHandler(GraphModelResult[] results) {
        this.results = results;
        this.count = 0;
    }

    @Override
    public GraphModel next() {
        if (results == null || count == results.length) {
            return null;
        }
        return results[count++].getGraph();
    }

}
