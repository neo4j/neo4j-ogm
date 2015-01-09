package org.neo4j.ogm.cypher.compiler;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class NewRelationshipBuilder extends RelationshipBuilder {

    private String startNodeIdentifier;
    private String endNodeIdentifier;

    @Override
    public void relate(String startNodeIdentifier, String endNodeIdentifier) {
        this.startNodeIdentifier = startNodeIdentifier;
        this.endNodeIdentifier = endNodeIdentifier;
    }

    @Override
    public boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack) {
        // don't emit anything if this relationship isn't used to link any nodes
        // admittedly, this isn't brilliant, as we'd ideally avoid creating the relationship in the first place
        if (this.startNodeIdentifier == null || this.endNodeIdentifier == null) {
            return false;
        }

        if (!varStack.isEmpty()) {
            queryBuilder.append(" WITH ").append(NodeBuilder.toCsv(varStack));
        }

        if (!varStack.contains(startNodeIdentifier)) {
            queryBuilder.append(" MATCH (");
            queryBuilder.append(startNodeIdentifier);
            queryBuilder.append(") WHERE id(");
            queryBuilder.append(startNodeIdentifier);
            queryBuilder.append(")=");
            queryBuilder.append(startNodeIdentifier.substring(1)); // existing nodes have an id. we pass it in as $id
            varStack.add(startNodeIdentifier);
        }

        if (!varStack.contains(endNodeIdentifier)) {
            queryBuilder.append(" MATCH (");
            queryBuilder.append(endNodeIdentifier);
            queryBuilder.append(") WHERE id(");
            queryBuilder.append(endNodeIdentifier);
            queryBuilder.append(")=");
            queryBuilder.append(endNodeIdentifier.substring(1)); // existing nodes have an id. we pass it in as $id
            varStack.add(endNodeIdentifier);
        }

        queryBuilder.append(" MERGE (");
        queryBuilder.append(startNodeIdentifier);
        queryBuilder.append(")-[:");
        queryBuilder.append(type);
        if (!this.props.isEmpty()) {
            // TODO: should update this to use query parameters properly like we do with nodes
            queryBuilder.append(" {");
            for (Entry<String, Object> relationshipProperty: this.props.entrySet()) {
                if (relationshipProperty.getValue() != null) {
                    queryBuilder.append(relationshipProperty.getKey()).append(':').append(relationshipProperty.getValue()).append(',');
                }
            }
            queryBuilder.setLength(queryBuilder.length() - 1);
            queryBuilder.append('}');
        }
        queryBuilder.append("]->(");
        queryBuilder.append(endNodeIdentifier);
        queryBuilder.append(")");

        return true;
    }

}
