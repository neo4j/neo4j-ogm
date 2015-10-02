package org.neo4j.ogm.driver.impl.result;

import org.neo4j.ogm.api.result.DriverResult;

/**
 * @author Vince Bickers
 */
public class ResultRowModel implements DriverResult<Object[]> {

    private Object[] row;

    public Object[] model() {
        return row;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRow(Object[] rowModel) {
        this.row = rowModel;
    }

}
