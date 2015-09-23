package org.neo4j.ogm.session.response.model;

import java.util.Collection;

/**
 * Encapsulates {@link org.neo4j.ogm.session.response.model.QueryStatisticsModel} and row data returned by a query.
 * @author Luanne Misquitta
 */
public class RowStatisticsModel {

    private Collection<Object> rows;
    private QueryStatisticsModel stats;

    public QueryStatisticsModel getStats() {
        return stats;
    }

    public void setRows(Collection<Object> rows) {
        this.rows = rows;
    }

    public void setStats(QueryStatisticsModel stats) {
        this.stats = stats;
    }

    public Collection<Object> getRows() {
        return rows;
    }
}
