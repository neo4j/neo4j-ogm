package org.neo4j.ogm.result;

import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.Query;
import org.neo4j.ogm.response.model.DefaultGraphModel;

/**
 * A result encapsulated in a GraphModel
 *
 * @author Vince Bickers
 */
public class ResultGraphModel implements Query<GraphModel> {

    private GraphModel graph;

    public GraphModel model() {
        return graph;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setGraph(DefaultGraphModel graph) {
        this.graph = graph;
    }

}
