package org.neo4j.ogm.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graphaware.graphmodel.neo4j.GraphModel;

import java.io.IOException;

public class GraphModelBuilder {

    private GraphModel graph;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private GraphModel getGraph() {
        return graph;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setGraph(GraphModel graph) {
        this.graph = graph;
    }

    public static GraphModel build(String json) throws IOException {
        GraphModelBuilder instance = objectMapper.readValue(json, GraphModelBuilder.class);
        return instance.getGraph();
    }
}
