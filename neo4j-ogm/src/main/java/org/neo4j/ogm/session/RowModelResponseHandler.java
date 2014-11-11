package org.neo4j.ogm.session;

public class RowModelResponseHandler implements Neo4jResponseHandler<RowModel> {

    private final RowModelResult[] results;
    private int count;

    public RowModelResponseHandler(RowModelResult[] results) {
        this.results = results;
        this.count = 0;
    }

    @Override
    public RowModel next() {
        if (results == null || count == results.length) {
            return null;
        }
        return new RowModel(results[count++].getRow());
    }

}
