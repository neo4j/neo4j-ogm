package org.neo4j.ogm.model;

import java.util.Collection;

/**
 * @author vince
 */
public interface RowStatisticsModel extends Iterable {

    public Collection getRows();
    public Statistics getStats();
}
