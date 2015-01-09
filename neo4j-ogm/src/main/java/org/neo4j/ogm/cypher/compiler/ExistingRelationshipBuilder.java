package org.neo4j.ogm.cypher.compiler;

import java.util.Map;
import java.util.Set;

class ExistingRelationshipBuilder extends RelationshipBuilder {

    private String startNodeIdentifier;
    private String endNodeIdentifier;

    ExistingRelationshipBuilder(Long relationshipId) {
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

        // TODO: replace all 'r' with this.reference() when it's working
        queryBuilder.append(" MATCH ()-[r]->() WHERE id(r)=").append(this.id);

        if (!this.props.isEmpty()) {
            queryBuilder.append(" SET ").append('r').append("+={").append('r').append("_props} ");
            parameters.put('r' + "_props", this.props);
        }

        return true;
    }

}
