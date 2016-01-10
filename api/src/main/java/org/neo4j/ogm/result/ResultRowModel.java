package org.neo4j.ogm.result;

import org.neo4j.ogm.model.Query;

/**
 * @author Vince Bickers
 */
public class ResultRowModel implements Query<Object[]> {

    private Object[] row;

    public Object[] queryResults() {
        return row;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRow(Object[] rowModel) {
        this.row = rowModel;
    }

}
