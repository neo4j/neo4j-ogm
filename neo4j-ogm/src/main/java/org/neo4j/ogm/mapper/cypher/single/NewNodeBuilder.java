package org.neo4j.ogm.mapper.cypher.single;

import java.util.List;
import java.util.Map;

/**
 * Renders Cypher appropriate for a new node that needs creating in the database.
 */
class NewNodeBuilder extends SingleQueryNodeBuilder {

    NewNodeBuilder(String variableName) {
        super(variableName);
    }

    @Override
    protected void renderTo(StringBuilder queryBuilder, Map<String, Object> parameters, List<String> varStack) {
        // MERGE is arguably safer than CREATE here in a multi-threaded environment, but is also slower
        queryBuilder.append(" CREATE ");
        queryBuilder.append('(');
        queryBuilder.append(this.variableName);
        for (String label : this.labels) {
            queryBuilder.append(":`").append(label).append('`');
        }
        queryBuilder.append('{').append(this.variableName).append("_props})");
        parameters.put(this.variableName + "_props", this.props);
        varStack.add(this.variableName);
    }

}