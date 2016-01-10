package org.neo4j.ogm.response.model;

import org.neo4j.ogm.model.QueryStatistics;
import org.neo4j.ogm.model.Result;

import java.util.Iterator;
import java.util.Map;

/**
 * @author vince
 */
public class QueryResultModel implements Result {

    private final Iterable<Map<String,Object>> result;
    private final QueryStatistics queryQueryStatistics;

    public QueryResultModel(Iterable<Map<String, Object>> result, QueryStatistics queryQueryStatistics) {
        this.result = result;
        this.queryQueryStatistics = queryQueryStatistics;
    }

    @Override
    public Iterator<Map<String,Object>> iterator() {
        return result.iterator();
    }

    @Override
    public Iterable<Map<String,Object>> queryResults() {
        return result;
    }

    @Override
    public QueryStatistics queryStatistics() {
        return queryQueryStatistics;
    }
}
