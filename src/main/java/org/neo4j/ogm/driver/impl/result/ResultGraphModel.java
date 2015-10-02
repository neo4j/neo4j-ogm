package org.neo4j.ogm.driver.impl.result;

import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.result.DriverResult;
import org.neo4j.ogm.driver.impl.model.GraphModel;

/**
 * A result encapsulated in a GraphModel
 *
 * @author Vince Bickers
 */
public class ResultGraphModel implements DriverResult<Graph> {

    private Graph graph;

    public Graph model() {
        return graph;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setGraph(GraphModel graph) {
        this.graph = graph;
    }

}
