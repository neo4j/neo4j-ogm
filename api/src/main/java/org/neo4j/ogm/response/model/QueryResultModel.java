package org.neo4j.ogm.response.model;

import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.model.Statistics;

import java.util.Iterator;
import java.util.Map;

/**
 * @author vince
 */
public class QueryResultModel implements Result {

    private final Iterable<Map<String,Object>> result;
    private final Statistics queryStatistics;

    public QueryResultModel(Iterable<Map<String, Object>> result, Statistics queryStatistics) {
        this.result = result;
        this.queryStatistics = queryStatistics;
    }

    @Override
    public Iterator<Map<String,Object>> iterator() {
        return result.iterator();
    }

    @Override
    public Iterable<Map<String,Object>> model() {
        return result;
    }

    @Override
    public Statistics statistics() {
        return queryStatistics;
    }
}
