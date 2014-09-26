package org.neo4j.ogm.mapper.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graphaware.graphmodel.neo4j.GraphModel;

import java.io.IOException;

public class GraphBuilder {

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
        GraphBuilder instance = objectMapper.readValue(json, GraphBuilder.class);
        return instance.getGraph();
    }
}
