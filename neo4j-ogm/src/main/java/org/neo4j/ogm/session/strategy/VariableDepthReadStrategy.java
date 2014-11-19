package org.neo4j.ogm.session.strategy;

import org.neo4j.graphmodel.Property;
import org.neo4j.ogm.mapper.cypher.GraphModelQuery;
import org.neo4j.ogm.session.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VariableDepthReadStrategy implements ReadStrategy {

    @Override
    public GraphModelQuery findOne(Long id, int depth) {
        int max = max(depth);
        int min = min(max);
        String qry = String.format("MATCH p=(n)-[*%d..%d]-(m) WHERE id(n) = { id } RETURN collect(distinct p)", min, max);
        return new GraphModelQuery(qry, Utils.map("id", id));
    }
    @Override
    public GraphModelQuery findAll(Collection<Long> ids, int depth) {
        int max = max(depth);
        int min = min(max);
        String qry=String.format("MATCH p=(n)-[*%d..%d]-(m) WHERE id(n) in { ids } RETURN collect(distinct p)", min, max);
        return new GraphModelQuery(qry, Utils.map("ids", ids));
    }

    @Override
    public GraphModelQuery findAll() {
        return new GraphModelQuery("MATCH p=()-->() RETURN p", Utils.map());
    }

    @Override
    public GraphModelQuery findByLabel(String label, int depth) {
        int max = max(depth);
        int min = min(max);
        String qry = String.format("MATCH p=(n:%s)-[*%d..%d]-(m) RETURN collect(distinct p)", label, min, max);
        return new GraphModelQuery(qry, Utils.map());
    }

    @Override
    public GraphModelQuery findByProperty(String label, Property<String, Object> property, int depth) {
        List<Property<String, Object>> properties = new ArrayList<>();
        properties.add(property);
        int max = max(depth);
        int min = min(max);
        String qry = String.format("MATCH p=(n:%s)-[*%d..%d]-(m) WHERE n.%s = { %s } RETURN collect(distinct p)", label, min, max, property.getKey(), property.getKey(), min, max);
        return new GraphModelQuery(qry, Utils.map(property.getKey(), property.asParameter()));
    }

    private int min(int depth) {
        return Math.min(1, depth);
    }

    private int max(int depth) {
        return Math.max(0, depth);
    }
}
