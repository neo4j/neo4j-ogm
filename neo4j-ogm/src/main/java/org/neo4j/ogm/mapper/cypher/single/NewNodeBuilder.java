package org.neo4j.ogm.mapper.cypher.single;

import java.util.List;
import java.util.Map;

/**
 * Renders Cypher appropriate for a new node that needs creating in the database.
 */
class NewNodeBuilder extends SingleQueryNodeBuilder {

    @Override
    protected void renderTo(StringBuilder queryBuilder, List<String> varStack) {
        // MERGE is arguably safer than CREATE here in a multi-threaded environment, but is also slower
        queryBuilder.append(" CREATE ");
        queryBuilder.append('(');
        queryBuilder.append(this.variableName);
        for (String label : this.labels) {
            queryBuilder.append(":`").append(label).append('`');
        }
        queryBuilder.append('{');
        for (Map.Entry<String, Object> string : this.props.entrySet()) {
            queryBuilder.append(string.getKey()).append(':');
            queryBuilder.append("\"").append(string.getValue()).append("\",");
        }
        queryBuilder.setLength(queryBuilder.length() - 1); // delete trailing comma
        queryBuilder.append('}').append(')');

        varStack.add(this.variableName);
    }

}