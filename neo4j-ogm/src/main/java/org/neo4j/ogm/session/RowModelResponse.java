package org.neo4j.ogm.session;

public class RowModelResponse {

    private Result[] results;
    private String[] errors;

    public Result[] getResults() {
        return results;
    }

    public void setResults(Result[] results) {
        this.results = results;
    }

    public String[] getErrors() {
        return errors;
    }

    public void setErrors(String[] errors) {
        this.errors = errors;
    }

    static class Result {

        private String[] columns;
        private RowModelResult[] data;

        public String[] getColumns() {
            return columns;
        }

        public void setColumns(String[] columns) {
            this.columns = columns;
        }

        public RowModelResult[] getData() {
            return data;
        }

        public void setData(RowModelResult[] data) {
            this.data = data;
        }

    }

}
