package org.neo4j.ogm.session.result;

public class RowModel {

    private Object[] values;

    public RowModel(Object[] values) {
        this.values = values;
    }

    public Object[] getValues() {
        return values;
    }
}