package org.neo4j.ogm.mapper.cypher.compiler;

import org.neo4j.ogm.entityaccess.FieldAccess;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;

import java.util.Map;
import java.util.Set;

/**
 * Renders Cypher appropriate for a node that already exists in the database and needs updating.
 */
class ExistingNodeBuilder extends NodeBuilder {

    ExistingNodeBuilder(String variableName) {
        super(variableName);
    }

    public NodeBuilder mapProperties(Object toPersist, ClassInfo classInfo) {
        for (FieldInfo propertyField : classInfo.propertyFields()) {
            String propertyName = propertyField.property();
            Object value = FieldAccess.read(classInfo.getField(propertyField), toPersist);
            addProperty(propertyName, value);
        }
        return this;
    }


    @Override
    public boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack) {

        // what about labels?
        if (this.props.isEmpty()) {
            return false;
        }

        if (!varStack.isEmpty()) {
            queryBuilder.append(" WITH ").append(toCsv(varStack));
        }

        varStack.add(this.cypherReference);

        queryBuilder.append(" MATCH (").append(this.cypherReference).append(")");
        queryBuilder.append(" WHERE id(").append(this.cypherReference).append(")=").append(this.cypherReference.substring(1));

        if (!this.labels.isEmpty() && !this.props.isEmpty()) {
            queryBuilder.append(" SET ");
        }
        // set the labels (at the moment we set all labels, not just new ones)
        if (!this.labels.isEmpty()) {
            queryBuilder.append(this.cypherReference);
            for (String label : this.labels) {
                queryBuilder.append(":`").append(label).append('`');
            }
            queryBuilder.append(", ");
        }

        if (!this.props.isEmpty()) {
            queryBuilder.append(this.cypherReference).append("+={").append(this.cypherReference).append("_props} ");
            parameters.put(this.cypherReference + "_props", this.props);
        }

        return true;

    }

}
