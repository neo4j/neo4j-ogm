package org.neo4j.ogm.cypher.compiler;

import java.util.Map;
import java.util.Set;

class ExistingRelationshipBuilder extends RelationshipBuilder {

    private final Long id;
    private String startNodeIdentifier;
    private String endNodeIdentifier;

    ExistingRelationshipBuilder(String variableName, Long relationshipId) {
        super(variableName);
        this.id = relationshipId;
    }

    @Override
    public void relate(String startNodeIdentifier, String endNodeIdentifier) {
        this.startNodeIdentifier = startNodeIdentifier;
        this.endNodeIdentifier = endNodeIdentifier;
    }

    @Override
    public boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack) {
        // admittedly, this isn't brilliant, as we'd ideally avoid creating the relationship in the first place
        // this doesn't make sense here because we don't even use the node identifiers for updating rels!
        if (this.startNodeIdentifier == null || this.endNodeIdentifier == null) {
            return false;
        }

        if (!varStack.isEmpty()) {
            queryBuilder.append(" WITH ").append(NodeBuilder.toCsv(varStack));
        }

        queryBuilder.append(" MATCH ()-[").append(this.reference).append("]->() WHERE id(")
                .append(this.reference).append(")=").append(this.id);

        if (!this.props.isEmpty()) {
            queryBuilder.append(" SET ").append(this.reference).append("+={").append(this.reference).append("_props} ");
            parameters.put(this.reference + "_props", this.props);
        }

        return true;
    }

}
