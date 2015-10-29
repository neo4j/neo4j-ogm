package org.neo4j.ogm.response.model;

import org.neo4j.ogm.model.RowStatisticsModel;
import org.neo4j.ogm.model.Statistics;

import java.util.Collection;
import java.util.Iterator;

/**
 * Encapsulates {@link StatisticsModel} and row data returned by a query.
 * @author Luanne Misquitta
 */
public class DefaultRowStatisticsModel implements RowStatisticsModel {

    private Collection rows;
    private Statistics stats;

    public Statistics getStats() {
        return stats;
    }

    public void setRows(Collection rows) {
        this.rows = rows;
    }

    public void setStats(Statistics stats) {
        this.stats = stats;
    }

    public Collection getRows() {
        return rows;
    }

    @Override
    public Iterator iterator() {
        return rows.iterator();
    }
}
