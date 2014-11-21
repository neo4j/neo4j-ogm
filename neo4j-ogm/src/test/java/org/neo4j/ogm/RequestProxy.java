package org.neo4j.ogm;

import org.neo4j.ogm.session.request.Neo4jRequestHandler;
import org.neo4j.ogm.session.response.Neo4jResponseHandler;

public abstract class RequestProxy implements Neo4jRequestHandler<String> {

    protected abstract String[] getResponse();

    public Neo4jResponseHandler<String> execute(String url, String request) {
        return new Response(getResponse());
    }

    static class Response implements Neo4jResponseHandler<String> {

        private final String[] jsonModel;
        private int count = 0;

        public Response(String[] jsonModel) {
            this.jsonModel = jsonModel;
        }

        public String next()  {
            if (count < jsonModel.length) {
                String json = jsonModel[count];
                count++;
                return json;
            }
            return null;
        }

        @Override
        public void close() {
            // nothing to do.
        }

        @Override
        public void initialiseScan(String token) {
            // nothing to do
        }

        @Override
        public String[] columns() {
            return new String[0];
        }
    }

}
