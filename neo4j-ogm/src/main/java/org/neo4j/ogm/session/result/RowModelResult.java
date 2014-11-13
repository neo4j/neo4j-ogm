package org.neo4j.ogm.session.result;

public class RowModelResult {

    private Object[] row;

    public Object[] getRow() {
        return row;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRow(Object[] rowModel) {
        this.row = rowModel;
    }

}
