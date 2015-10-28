package org.neo4j.ogm.result;

import org.neo4j.ogm.model.Graph;
import org.neo4j.ogm.model.Query;
import org.neo4j.ogm.response.model.GraphModel;

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
