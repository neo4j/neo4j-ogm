package org.neo4j.ogm.session;

import org.graphaware.graphmodel.neo4j.GraphModel;

public class GraphModelResult {

    private GraphModel graph;

    public GraphModel getGraph() {
        return graph;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setGraph(GraphModel graph) {
        this.graph = graph;
    }

}
