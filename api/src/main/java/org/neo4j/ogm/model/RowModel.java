package org.neo4j.ogm.model;

/**
 * @author vince
 */
public interface RowModel {

    Object[] getValues();
    String[] variables();
}
