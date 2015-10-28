package org.neo4j.ogm.response.model;

import org.neo4j.ogm.model.RowStatistics;
import org.neo4j.ogm.model.Statistics;

import java.util.Collection;

/**
 * Encapsulates {@link StatisticsModel} and row data returned by a query.
 * @author Luanne Misquitta
 */
public class RowStatisticsModel implements RowStatistics {

    private Collection<Object> rows;
    private Statistics stats;

    public Statistics getStats() {
        return stats;
    }

    public void setRows(Collection<Object> rows) {
        this.rows = rows;
    }

    public void setStats(Statistics stats) {
        this.stats = stats;
    }

    public Collection<Object> getRows() {
        return rows;
    }
}
