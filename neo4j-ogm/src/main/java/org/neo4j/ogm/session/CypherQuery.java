package org.neo4j.ogm.session;

import org.graphaware.graphmodel.neo4j.Property;

import java.util.Collection;

public class CypherQuery implements Query {

    @Override
    public String findOne(Long id) {
        return String.format("MATCH p=(n)--(m) WHERE id(n) = %d RETURN p", id);
    }

    @Override
    public String findAll(Collection<Long> ids) {
        return String.format("MATCH p=(n)--(m) WHERE id(n) in [%s] RETURN p", idList(ids));
    }

    @Override
    public String findAll() {
        return "MATCH p=()-->() RETURN p";
    }

    @Override
    public String findByLabel(String label) {
        return String.format("MATCH p=(n:%s)--(m) RETURN p", label);
    }

    @Override
    public String delete(Long id) {
        return String.format("MATCH (n) WHERE id(n) = %d OPTIONAL MATCH (n)-[r]-() DELETE r, n", id);
    }

    @Override
    public String deleteAll(Collection<Long> ids) {
        return String.format("MATCH (n) WHERE id(n) in [%s] OPTIONAL MATCH (n)-[r]-() DELETE r, n", idList(ids));
    }

    @Override
    public String purge() {
        return "MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n";
    }

    @Override
    public String deleteByLabel(String label) {
        return String.format("MATCH (n:%s) OPTIONAL MATCH (n)-[r]-() DELETE r, n", label);
    }

    @Override
    public String updateProperties(Long identity, Collection<Property<String, Object>> properties) {
        return String.format("MATCH (n) WHERE id(n) = %d %s", identity, setProperties(properties));

    }

    private String idList(Collection<Long> ids) {
        StringBuilder builder = new StringBuilder();
        for (Long id : ids) {
            builder.append(",");
            builder.append(id);
        }
        return builder.toString().substring(1);
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
        sb.setLength(sb.length()-1);
        return sb.toString();
    }

}
