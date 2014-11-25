package org.neo4j.ogm.session.strategy;

import org.neo4j.graphmodel.Property;
import org.neo4j.ogm.mapper.cypher.statements.GraphModelQuery;
import org.neo4j.ogm.mapper.cypher.statements.ParameterisedStatement;
import org.neo4j.ogm.mapper.cypher.statements.RowModelQuery;
import org.neo4j.ogm.session.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DepthOneStrategy implements ReadStrategy, WriteStrategy {

    public GraphModelQuery findOne(Long id) {
        return new GraphModelQuery("MATCH p=(n)--(m) WHERE id(n) = { id } RETURN p", Utils.map("id", id));
    }

    @Override
    public GraphModelQuery findOne(Long id, int depth) {
        return findOne(id);
    }

    public GraphModelQuery findAll(Collection<Long> ids) {
        return new GraphModelQuery("MATCH p=(n)--(m) WHERE id(n) in { ids } RETURN p", Utils.map("ids", ids));
    }

    @Override
    public GraphModelQuery findAll(Collection<Long> ids, int depth) {
        return findAll(ids);
    }

    @Override
    public GraphModelQuery findAll() {
        return new GraphModelQuery("MATCH p=()-->() RETURN p", Utils.map());
    }

    public GraphModelQuery findByLabel(String label) {
        return new GraphModelQuery(String.format("MATCH p=(n:%s)--(m) RETURN p", label), Utils.map());
    }

    @Override
    public GraphModelQuery findByLabel(String label, int depth) {
        return findByLabel(label);
    }

    public GraphModelQuery findByProperty(String label, Property<String, Object> property) {
        List<Property<String, Object>> properties = new ArrayList<>();
        properties.add(property);
        return new GraphModelQuery(String.format("MATCH p=(n:%s { %s } )--(m) return p", label, property.getKey()), Utils.map(property.getKey(), property.asParameter()));
    }

    @Override
    public GraphModelQuery findByProperty(String label, Property<String, Object> property, int depth) {
        return findByProperty(label, property);
    }

    @Override
    public ParameterisedStatement delete(Long id) {
        return new ParameterisedStatement("MATCH (n) WHERE id(n) = { id } OPTIONAL MATCH (n)-[r]-() DELETE r, n", Utils.map("id",id));
    }

    @Override
    public ParameterisedStatement deleteAll(Collection<Long> ids) {
        return new ParameterisedStatement("MATCH (n) WHERE id(n) in { ids } OPTIONAL MATCH (n)-[r]-() DELETE r, n", Utils.map("ids", ids));
    }

    @Override
    public ParameterisedStatement purge() {
        return new ParameterisedStatement("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n", Utils.map());
    }

    @Override
    public ParameterisedStatement deleteByLabel(String label) {
        return new ParameterisedStatement(String.format("MATCH (n:%s) OPTIONAL MATCH (n)-[r]-() DELETE r, n", label), Utils.map());
    }

    @Override
    public ParameterisedStatement updateProperties(Long identity, Collection<Property<String, Object>> properties) {
        return new ParameterisedStatement(String.format("MATCH (n) WHERE id(n) = { id } %s", setProperties(properties)), Utils.map("id", identity));

    }

    @Override
    public RowModelQuery createNode(Collection<Property<String, Object>> properties, Collection<String> labels) {
        return new RowModelQuery(String.format("CREATE (n%s { properties }) return id(n)", setLabels(labels)), Utils.mapCollection("properties", properties));
    }

    private String setLabels(Collection<String> labels) {
        StringBuilder sb = new StringBuilder();
        for (String label : labels) {
            sb.append(":");
            sb.append(label);
        }
        return sb.toString();
    }

    private String setProperties(Collection<Property<String, Object>> properties) {
        StringBuilder sb = new StringBuilder();
        if (properties.isEmpty()) {
            return "";
        }

        sb.append("SET ");
        for (Property<String, Object> property : properties) {
            sb.append("n.");
            sb.append(property.getKey());
            sb.append("=");
            Object value = property.getValue();
            if (value instanceof String) {
                sb.append("\\\""); // must double-escape these quotes
                sb.append(value);
                sb.append("\\\"");
            } else {
                sb.append(value);
            }
            sb.append(",");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

}
