package org.neo4j.ogm.api.model;

import org.neo4j.ogm.driver.impl.model.StatisticsModel;

import java.util.Collection;

/**
 * @author vince
 */
public interface RowStatistics {

    public Collection<Object> getRows();
    public Statistics getStats();
}
