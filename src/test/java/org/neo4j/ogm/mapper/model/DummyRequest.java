package org.neo4j.ogm.mapper.model;

import org.graphaware.graphmodel.neo4j.GraphModel;

public abstract class DummyRequest {

    protected ResponseStream responseStream;

    protected void setResponse(String[] model) {
        this.responseStream = new ResponseStream(model);
    }

    public ResponseStream getResponse() {
        return responseStream;
    }

    public static class ResponseStream {

        private final String[] jsonModel;
        private int count = 0;

        public ResponseStream(String[] jsonModel) {
            this.jsonModel = jsonModel;
        }

        public GraphModel next() throws Exception {
            if (hasNext()) {
                String json = jsonModel[count];
                //System.out.println("response: " + json);
                count++;
                return GraphBuilder.build(json);
            }
            return null;
        }

        private boolean hasNext() {
            return count < jsonModel.length;
        }

    }
}
