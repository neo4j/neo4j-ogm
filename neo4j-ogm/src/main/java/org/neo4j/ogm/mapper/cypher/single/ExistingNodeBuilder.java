package org.neo4j.ogm.mapper.cypher.single;

import java.util.List;
import java.util.Map;

/**
 * Renders Cypher appropriate for a node that already exists in the database and needs updating.
 */
class ExistingNodeBuilder extends SingleQueryNodeBuilder {

    @Override
    protected void renderTo(StringBuilder queryBuilder, Map<String, Object> parameters, List<String> varStack) {
        queryBuilder.append(" MATCH (").append(this.variableName).append(")");
        // now then, we should really have parameters returned along with the statements, shouldn't we?
        queryBuilder.append(" WHERE id(").append(this.variableName).append(")=").append(this.nodeId);
        queryBuilder.append(" SET ");
        if (!this.labels.isEmpty()) {
            queryBuilder.append(this.variableName);
            for (String label : this.labels) {
                queryBuilder.append(":`").append(label).append('`');
            }
            queryBuilder.append(", ");
        }
        queryBuilder.append(this.variableName).append("={").append(this.variableName).append("_props} ");
        parameters.put(this.variableName + "_props", this.props);

        varStack.add(this.variableName);
    }

}
