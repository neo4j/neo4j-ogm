package org.neo4j.ogm.api.result;

import org.neo4j.ogm.api.model.Query;

/**
 * @author Vince Bickers
 */
public class ResultRowModel implements Query<Object[]> {

    private Object[] row;

    public Object[] model() {
        return row;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRow(Object[] rowModel) {
        this.row = rowModel;
    }

}
