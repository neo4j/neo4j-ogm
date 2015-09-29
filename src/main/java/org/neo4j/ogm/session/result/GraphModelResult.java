package org.neo4j.ogm.session.result;

import org.neo4j.ogm.session.response.model.GraphModel;

/**
 * A result encapsulated in a GraphModel
 *
 * @author Vince Bickers
 */
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
