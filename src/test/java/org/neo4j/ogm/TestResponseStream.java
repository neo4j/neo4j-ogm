package org.neo4j.ogm;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.mapper.cypher.ResponseStream;
import org.neo4j.ogm.mapper.model.GraphBuilder;

public class TestResponseStream implements ResponseStream<GraphModel> {

    private int count = 0;
    private String responseText;

    public TestResponseStream(int records, String responseText) {
        this.count = records;
        this.responseText = responseText;
    }

    public GraphModel next() {
        if (hasNext()) {
            count--;
            String json = nextResponse();
            try {
                return GraphBuilder.build(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public boolean hasNext() {
        return count > 0;
    }

    private String nextResponse() {
        return responseText;
    }

}

