package org.neo4j.ogm.api.result;

import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.model.Query;
import org.neo4j.ogm.api.response.model.GraphModel;

/**
 * A result encapsulated in a GraphModel
 *
 * @author Vince Bickers
 */
public class ResultGraphModel implements Query<Graph> {

    private Graph graph;

    public Graph model() {
        return graph;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setGraph(GraphModel graph) {
        this.graph = graph;
    }

}
