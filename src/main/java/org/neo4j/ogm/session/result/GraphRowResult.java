package org.neo4j.ogm.session.result;

import org.neo4j.ogm.session.response.model.GraphModel;

/**
 * Represents a single row in a query response which returns both graph and row data.
 *
 * @author Luanne Misquitta
 */
public class GraphRowResult {

    private GraphModel graph;
    private Object[] row;

    public GraphRowResult(GraphModel graph, Object[] row) {
        this.graph = graph;
        this.row = row;
    }

    public GraphModel getGraph() {
        return graph;
    }

    public Object[] getRow() {
        return row;
    }
}
