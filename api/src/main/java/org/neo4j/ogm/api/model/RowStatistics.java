package org.neo4j.ogm.api.model;

import java.util.Collection;

/**
 * @author vince
 */
public interface RowStatistics {

    public Collection<Object> getRows();
    public Statistics getStats();
}
