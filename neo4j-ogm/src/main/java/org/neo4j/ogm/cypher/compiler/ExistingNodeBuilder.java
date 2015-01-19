package org.neo4j.ogm.cypher.compiler;

import org.neo4j.ogm.entityaccess.EntityAccessStrategy;
import org.neo4j.ogm.entityaccess.PropertyReader;
import org.neo4j.ogm.metadata.info.ClassInfo;

import java.util.Map;
import java.util.Set;

/**
 * Renders Cypher appropriate for a node that already exists in the database and needs updating.
 */
class ExistingNodeBuilder extends NodeBuilder {

    ExistingNodeBuilder(String variableName) {
        super(variableName);
    }

    @Override
    public NodeBuilder mapProperties(Object entity, ClassInfo classInfo, EntityAccessStrategy objectAccessStrategy) {
        for (PropertyReader propertyReader : objectAccessStrategy.getPropertyReaders(classInfo)) {
            Object value = propertyReader.read(entity);
            addProperty(propertyReader.propertyName(), value);
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

        varStack.add(this.reference());

        queryBuilder.append(" MATCH (").append(this.reference()).append(")");
        queryBuilder.append(" WHERE id(").append(this.reference()).append(")=").append(this.reference().substring(1));

        if (!this.labels.isEmpty() && !this.props.isEmpty()) {
            queryBuilder.append(" SET ");
        }
        // set the labels (at the moment we set all labels, not just new ones)
        if (!this.labels.isEmpty()) {
            queryBuilder.append(this.reference());
            for (String label : this.labels) {
                queryBuilder.append(":`").append(label).append('`');
            }
            queryBuilder.append(", ");
        }

        if (!this.props.isEmpty()) {
            queryBuilder.append(this.reference()).append("+={").append(this.reference()).append("_props} ");
            parameters.put(this.reference() + "_props", this.props);
        }

        return true;

    }

}
