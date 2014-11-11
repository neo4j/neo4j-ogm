package org.neo4j.ogm.session;

public class GraphModelResponse {

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
        private GraphModelResult[] data;

        public String[] getColumns() {
            return columns;
        }

        public void setColumns(String[] columns) {
            this.columns = columns;
        }

        public GraphModelResult[] getData() {
            return data;
        }

        public void setData(GraphModelResult[] data) {
            this.data = data;
        }
    }
}


