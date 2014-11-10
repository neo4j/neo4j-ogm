package org.neo4j.ogm.mapper.cypher.single;

import java.util.List;
import java.util.Map;

/**
 * Renders Cypher appropriate for a node that already exists in the database and needs updating.
 */
class ExistingNodeBuilder extends SingleQueryNodeBuilder {

    @Override
    protected void renderTo(StringBuilder queryBuilder, List<String> varStack) {
        queryBuilder.append(" MATCH (").append(this.variableName).append(")");
        // now then, we should really have parameters returned along with the statements, shouldn't we?
        queryBuilder.append(" WHERE id(").append(this.variableName).append(")=").append(this.nodeId);
        queryBuilder.append(" SET ");
        if (!this.labels.isEmpty()) {
            queryBuilder.append(this.variableName);
            for (String label : this.labels) {
                queryBuilder.append(':').append(label);
            }
            queryBuilder.append(", ");
        }
        for (Map.Entry<String, Object> string : this.props.entrySet()) {
            //TODO: use a DSL for value escaping if nothing else
            queryBuilder.append(this.variableName).append('.').append(string.getKey()).append('=');
            queryBuilder.append("\"").append(string.getValue()).append("\",");
        }
        queryBuilder.setLength(queryBuilder.length() - 1); // delete trailing comma
        queryBuilder.append(' ');

        varStack.add(this.variableName);
    }

}