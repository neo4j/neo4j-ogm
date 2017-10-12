package org.neo4j.ogm.cypher.function;

import org.neo4j.ogm.cypher.Filter;

import java.util.HashMap;
import java.util.Map;

public class InCollectionComparison implements FilterFunction<Object> {

    private final Object value;
    private Filter filter;

    public InCollectionComparison(Object value) {
        this.value = value;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String expression(String nodeIdentifier) {
        return String.format("ANY(collectionFields IN {`%s`} WHERE collectionFields in %s.`%s`) ",
            filter.uniqueParameterName(), nodeIdentifier, filter.getPropertyName());
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> map = new HashMap<>();
        map.put(filter.uniqueParameterName(), filter.getTransformedPropertyValue());
        return map;
    }
}
