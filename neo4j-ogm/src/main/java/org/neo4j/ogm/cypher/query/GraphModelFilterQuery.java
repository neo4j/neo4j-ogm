package org.neo4j.ogm.cypher.query;

import org.neo4j.ogm.annotation.Property;

import java.util.Map;

public class GraphModelFilterQuery extends GraphModelQuery {

    final Property filter;

    public GraphModelFilterQuery(Property filter, String cypher, Map<String, ? extends Object> parameters) {
        super(cypher, parameters);
        this.filter = filter;
    }


}
