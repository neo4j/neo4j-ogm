package org.neo4j.ogm.session;

import org.graphaware.graphmodel.neo4j.GraphModel;

import java.io.BufferedReader;
import java.io.IOException;

public class GraphModelResponseHandler implements Neo4jResponseHandler<GraphModel> {

    private final BufferedReader reader;

    public GraphModelResponseHandler(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public GraphModel next() {
        try {
            String response = reader.readLine();
            if (response != null) {
                return GraphModelBuilder.build(response);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
