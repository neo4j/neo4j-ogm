package org.neo4j.ogm.cypher.compiler;

import org.neo4j.ogm.entityaccess.ObjectAccessStrategy;
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

    @Override
    public NodeBuilder mapProperties(Object toPersist, ClassInfo classInfo, ObjectAccessStrategy objectAccessStrategy) {
        // FIXME: using fields here doesn't guarantee we get the correct corresponding property name and will actually
        // rely on the default strategy implementation to rescue the situation!
        for (FieldInfo propertyField : classInfo.propertyFields()) {
            String propertyName = propertyField.property();
            Object value = objectAccessStrategy.getPropertyReader(classInfo, propertyName).read(toPersist);
            if (value != null) {
                addProperty(propertyName, value);
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