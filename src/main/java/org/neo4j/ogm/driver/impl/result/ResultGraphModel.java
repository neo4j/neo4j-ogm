package org.neo4j.ogm.driver.impl.result;

import org.neo4j.ogm.driver.impl.model.GraphModel;
import org.neo4j.ogm.driver.api.result.DriverResult;

/**
 * A result encapsulated in a GraphModel
 *
 * @author Vince Bickers
 */
public class ResultGraphModel implements DriverResult<GraphModel> {

    private GraphModel graph;

    public GraphModel model() {
        return graph;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setGraph(GraphModel graph) {
        this.graph = graph;
    }

}
