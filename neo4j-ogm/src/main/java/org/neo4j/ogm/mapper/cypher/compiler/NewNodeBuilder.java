package org.neo4j.ogm.mapper.cypher.compiler;

import org.neo4j.ogm.entityaccess.FieldAccess;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;

import java.util.Map;
import java.util.Set;

/**
 * Renders Cypher appropriate for a new node that needs creating in the database.
 */
class NewNodeBuilder extends NodeBuilder {

    NewNodeBuilder(String variableName) {
        super(variableName);
    }

    public NodeBuilder mapProperties(Object toPersist, ClassInfo classInfo) {

        for (FieldInfo propertyField : classInfo.propertyFields()) {
            String propertyName = propertyField.property();
            Object value = FieldAccess.read(classInfo.getField(propertyField), toPersist);
            if (value != null) {
                addProperty(propertyName, value);
            }
        }
        return this;
    }

    @Override
    public boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack) {

        queryBuilder.append('(');
        queryBuilder.append(this.cypherReference);
        for (String label : this.labels) {
            queryBuilder.append(":`").append(label).append('`');
        }
        if (!this.props.isEmpty()) {
            queryBuilder.append('{').append(this.cypherReference).append("_props}");
            parameters.put(this.cypherReference + "_props", this.props);
        }
        queryBuilder.append(')');
        varStack.add(this.cypherReference);

        return true;
    }

}