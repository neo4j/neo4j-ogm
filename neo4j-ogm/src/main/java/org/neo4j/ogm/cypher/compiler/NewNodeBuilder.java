package org.neo4j.ogm.cypher.compiler;

import org.neo4j.ogm.entityaccess.ObjectAccessStrategy;
import org.neo4j.ogm.entityaccess.PropertyReader;
import org.neo4j.ogm.metadata.info.ClassInfo;

import java.util.Map;
import java.util.Set;

/**
 * Renders Cypher appropriate for a new node that needs creating in the database.
 */
class NewNodeBuilder extends NodeBuilder {

    NewNodeBuilder(String variableName) {
        super(variableName);
    }

    @Override
    public NodeBuilder mapProperties(Object toPersist, ClassInfo classInfo, ObjectAccessStrategy objectAccessStrategy) {
        for (PropertyReader propertyReader : objectAccessStrategy.getPropertyReaders(classInfo)) {
            Object value = propertyReader.read(toPersist);
            if (value != null) {
                addProperty(propertyReader.propertyName(), value);
            }
        }
        return this;
    }

    @Override
    public boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack) {

        queryBuilder.append('(');
        queryBuilder.append(this.reference());
        for (String label : this.labels) {
            queryBuilder.append(":`").append(label).append('`');
        }
        if (!this.props.isEmpty()) {
            queryBuilder.append('{').append(this.reference()).append("_props}");
            parameters.put(this.reference() + "_props", this.props);
        }
        queryBuilder.append(')');
        varStack.add(this.reference());

        return true;
    }

}