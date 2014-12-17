package org.neo4j.ogm.session.response;

public class EmptyResponse implements Neo4jResponse<String> {
    @Override
    public String next() {
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public void initialiseScan(String token) {
    }

    @Override
    public String[] columns() {
        return new String[0];
    }
    @Override
    public int rowId() {
        return -1;
    }
}
